package com.faraz.dictionary.aop;

import com.faraz.dictionary.entity.Dictionary;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Objects.nonNull;

@Aspect
@Component
public class DatabaseAop {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseAop.class);

  @Around("bean(dictionaryRepository) && execution(* saveAll(..))")
  public Object interceptDictionaryRepositorySaveAll(ProceedingJoinPoint pjp) throws Throwable {
    logger.info("Start of Servicing " + ((MethodSignature) pjp.getSignature()).getMethod());
    massage(pjp.getArgs());
    Object retVal = pjp.proceed();
    massage(new Object[]{retVal});
    logger.info("End of Servicing " + ((MethodSignature) pjp.getSignature()).getMethod());
    return retVal;
  }

  @Around("bean(dictionaryRepository) && execution(* findAll(..))")
  public Object interceptDictionaryRepositoryFindAll(ProceedingJoinPoint pjp) throws Throwable {
    logger.info("Start of Servicing " + ((MethodSignature) pjp.getSignature()).getMethod());
    Object retVal = pjp.proceed();
    massage(new Object[]{retVal});
    logger.info("End of Servicing " + ((MethodSignature) pjp.getSignature()).getMethod());
    return retVal;
  }

  @Around("bean(mongoTemplate) && (execution(* save(..)) || execution(* insert(..)))")
  public Object interceptMongoTemplateInsertions(ProceedingJoinPoint pjp) throws Throwable {
    logger.info("Start of Servicing " + ((MethodSignature) pjp.getSignature()).getMethod());
    Object[] objects = pjp.getArgs();
    massage(objects);
    Object retVal = pjp.proceed();
    massage(new Object[]{retVal});
    logger.info("End of Servicing " + ((MethodSignature) pjp.getSignature()).getMethod());
    return retVal;
  }

  @Around("bean(mongoTemplate) && (execution(* find(..)) || execution(* findAll(..)))")
  public Object interceptMongoTemplateFind(ProceedingJoinPoint pjp) throws Throwable {
    logger.info("Start of Servicing " + ((MethodSignature) pjp.getSignature()).getMethod());
    Object retVal = pjp.proceed();
    massage(new Object[]{retVal});
    logger.info("End of Servicing " + ((MethodSignature) pjp.getSignature()).getMethod());
    return retVal;
  }

  private void massage(Object[] args) {
    if (args[0] instanceof Collection) {
      Collection col = (Collection) args[0];
      for (Object o : col) {
        massageDictionary(o);
      }
    } else if (args[0] instanceof Dictionary) {
      massageDictionary(args[0]);
    } else if (args[0] instanceof PageImpl){
      for(Object o : ((PageImpl<Dictionary>) args[0]).getContent()){
        massageDictionary(o);
      }
    } else {
      throw new RuntimeException("Unknown type " + args[0].getClass());
    }
  }

  private void massageDictionary(Object o) {
    Dictionary dictionary = (Dictionary) o;
    if (nonNull(dictionary.getWord())) {
      dictionary.setWord(dictionary.getWord().trim().toLowerCase());
    }
  }
}
