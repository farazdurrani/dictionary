package com.faraz.dictionary.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Dictionary;

import static java.util.Objects.nonNull;

@Aspect
@Component
public class DatabaseAop {
  @Around("execution(* com.faraz.dictionary.repository.DictionaryRepository.*(..))")
  public Object interceptFind(ProceedingJoinPoint pjp) throws Throwable {
//    MethodInvocationProceedingJoinPoint joinPoint = (MethodInvocationProceedingJoinPoint) pjp
//    CodeSignature methodSignature = (CodeSignature) pjp.getSignature();
    Object[] obj = pjp.getArgs();
    if (nonNull(obj) && nonNull(obj[0])) {
      if (obj[0] instanceof Collection) {
          Collection col = (Collection) obj[0];
          for(Object o : col){
            Dictionary dictionary = (Dictionary) o;

          }
      } else if(obj[0] instanceof String){
        String col = (String) obj[0];
      }
    }
    Object retVal = pjp.proceed();
    return retVal;
  }

  @Around("bean(mongoTemplate) && (execution(* save(..)) || execution(* insert(..)) " +
      "|| execution(* find*(..)))")
  public Object intercepInsertions(ProceedingJoinPoint pjp) throws Throwable {
    Object [] objects = pjp.getArgs();
    Object retVal = pjp.proceed();
    return retVal;
  }
}
