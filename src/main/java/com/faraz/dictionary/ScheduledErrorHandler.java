package com.faraz.dictionary;


import com.faraz.dictionary.service.EmailService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.ErrorHandler;

public class ScheduledErrorHandler implements ErrorHandler {

  private final EmailService emailService;

  public ScheduledErrorHandler(EmailService emailService) {this.emailService = emailService;}

  @Override
  public void handleError(Throwable t) {
    String subject = "Something went wrong in scheduled tasks";
    String body = ExceptionUtils.getStackTrace(t);
    try {
      emailService.sendEmail(subject, body);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
