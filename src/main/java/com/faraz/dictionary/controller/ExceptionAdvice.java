package com.faraz.dictionary.controller;

import com.faraz.dictionary.service.EmailService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionAdvice {

  private final EmailService emailService;

  public ExceptionAdvice(EmailService emailService) {this.emailService = emailService;}

  @ExceptionHandler(Exception.class)
  public void handleException(Exception t) {
    String subject = "Something went wrong when looking up definition";
    String body = ExceptionUtils.getStackTrace(t);
    try {
      emailService.sendEmail(subject, body);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
