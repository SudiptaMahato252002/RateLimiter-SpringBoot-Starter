package com.example.RateLimiter.starter.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.RateLimiter.starter.annotation.RateLimit;
import com.example.RateLimiter.starter.exception.RateLimitExceededException;
import com.example.RateLimiter.starter.model.RateLimiterAlgorithm;
import com.example.RateLimiter.starter.model.RateLimiterResult;
import com.example.RateLimiter.starter.service.RateLimiterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@RequiredArgsConstructor
@Component
public class RateLimitAspect 
{
    private final RateLimiterService service;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.example.RateLimiter.starter.annotation.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable
    {

        MethodSignature signature=(MethodSignature)joinPoint.getSignature();
        Method method=signature.getMethod();
        RateLimit rateLimit=method.getAnnotation(RateLimit.class);

        String key=buildKey(rateLimit, joinPoint);
        RateLimiterResult result;

        if(rateLimit.algorithm()==RateLimiterAlgorithm.FIXED_WINDOW)
        {
            result=service.allowRequest(key, rateLimit.limit(), rateLimit.windowSeconds(),rateLimit.algorithm());
        }
        else
        {
            result=service.allowRequestTokenBucket();
        }

        addRateLimitHeaders(result);

        if (!result.isAllowed()) {
            log.warn("Rate limit exceeded for key: {}, algorithm: {}", 
                key, rateLimit.algorithm());
            
            if (rateLimit.throwException()) {
                throw new RateLimitExceededException(rateLimit.errorMessage(), result);
            }
        }

        log.debug("Rate limit check passed for key: {}, remaining: {}", 
            key, result.getRemaining());

        return joinPoint.proceed();


    }

    private String buildKey(RateLimit rateLimit,ProceedingJoinPoint joinPoint)
    {
         String baseKey=rateLimit.key();
            if (baseKey.contains("#{")) {
               baseKey = evaluateSpelExpression(baseKey, joinPoint);
            }

            if(!rateLimit.keyParam().isEmpty())
            {
                Object paramValue=getParamsValue(rateLimit.keyParam(), joinPoint);
                if (paramValue != null) {
                   baseKey = baseKey + ":" + paramValue;
                }
            }

        try 
        {
           
            ServletRequestAttributes attributes=(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            if(attributes!=null)
            {
                HttpServletRequest request=attributes.getRequest();
                String userId=request.getHeader("X-User-Id");
                if (userId != null && !userId.isEmpty()) 
                {
                    baseKey = baseKey + ":" + userId;
                }
                else
                {
                    String ipAddress=getClientIpAddress(request);
                    baseKey=baseKey+":"+ipAddress;
                }
            }
        } 
        catch (Exception e) 
        {
            log.debug("Not in web context, using base key only");
        }
        return baseKey;
    }
    
    private Object getParamsValue(String paramName,ProceedingJoinPoint joinPoint)
    {
        MethodSignature signature=(MethodSignature) joinPoint.getSignature();
        Parameter[] params=signature.getMethod().getParameters();
        Object agrs[]=joinPoint.getArgs();

        for(int i=0;i<params.length;i++)
        {
            if(params[i].getName().equals(paramName))
            {
                return agrs[i];
            }
        }
        return null;

    } 

    private String evaluateSpelExpression(String expression,ProceedingJoinPoint joinPoint)
    {
        try 
        {
            StandardEvaluationContext context=new StandardEvaluationContext();
            MethodSignature signature=(MethodSignature)joinPoint.getSignature();
            String[] parameterNames=signature.getParameterNames();
            Object[] arguments=joinPoint.getArgs();

            for(int i=0;i<parameterNames.length;i++)
            {
                context.setVariable(parameterNames[i], arguments[i]);
            }
            return parser.parseExpression(expression).getValue(context,String.class);
        } 
        catch (Exception e) 
        {
            log.error("Error evaluating SpEL expression: {}", expression, e);
            return expression;
        }

    }

    private String getClientIpAddress(HttpServletRequest request)
    {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for(String header:headers)
        {
            String ip=request.getHeader(header);
            if(ip!=null&&!ip.isEmpty()&&!"unknown".equalsIgnoreCase(ip))
            {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private void addRateLimitHeaders(RateLimiterResult result)
    {
        try 
        {
            ServletRequestAttributes attributes=(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            if(attributes!=null)
            {
                HttpServletResponse response=attributes.getResponse();
                if(response!=null)
                {
                    response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
                    response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
                    response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetInMs()));

                    if(!result.isAllowed())
                    {
                        response.setHeader("Retry-After", String.valueOf(result.getRetryAfterMs()/1000));
                    }
                }
            }
            
        } 
        catch (Exception e) 
        {
            log.debug("Not in web context, skipping headers");
        }
    }

}
