package com.faraz.dictionary.aop;

import com.faraz.dictionary.entity.Dictionary;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Objects.nonNull;

@Aspect
@Component
public class DatabaseAop {
  @Around("bean(dictionaryRepository) && execution(* saveAll(..))")
  public Object interceptSaveAll(ProceedingJoinPoint pjp) throws Throwable {
    massage(pjp.getArgs());
    Object retVal = pjp.proceed();
    massage(new Object[]{retVal});
    return retVal;
  }

  @Around("bean(dictionaryRepository) && execution(* findAll(..))")
  public Object interceptFindAll(ProceedingJoinPoint pjp) throws Throwable {
    Object retVal = pjp.proceed();
    massage(new Object[]{retVal});
    return retVal;
  }

  private void massage(Object[] args) {
    Collection col = (Collection) args[0];
    for (Object o : col) {
      Dictionary dictionary = (Dictionary) o;
      if (nonNull(dictionary.getWord())) {
        dictionary.setWord(dictionary.getWord().trim().toLowerCase());
      }
    }
  }

  @Around("bean(mongoTemplate) && (execution(* save(..)) || execution(* insert(..)))")
  public Object intercepInsertions(ProceedingJoinPoint pjp) throws Throwable {
    Object[] objects = pjp.getArgs();
    Object retVal = pjp.proceed();
    return retVal;
  }
}
