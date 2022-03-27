package com.faraz.dictionary.controller;

import com.faraz.dictionary.service.EmailService;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class HealthController {

  private final RestTemplate restTemplate;
  private final MongoTemplate mongoTemplate;
  private final String merriamWebsterKey;
  private final String merriamWebsterUrl;
  private final String freeDictionaryEndpoint;
  private final EmailService emailService;

  public HealthController(RestTemplate restTemplate, MongoTemplate mongoTemplate,
                          @Value("${dictionary.merriamWebster.key}") String merriamWebsterKey,
                          @Value("${dictionary.merriamWebster.url}") String merriamWebsterUrl,
                          @Value("${dictionary.freeDictionary.url}") String freeDictionaryEndpoint,
                          EmailService emailService) {
    this.restTemplate = restTemplate;
    this.mongoTemplate = mongoTemplate;
    this.merriamWebsterKey = merriamWebsterKey;
    this.merriamWebsterUrl = merriamWebsterUrl;
    this.freeDictionaryEndpoint = freeDictionaryEndpoint;
    this.emailService = emailService;
  }

  @GetMapping("/health")
  public String health() throws MailjetSocketTimeoutException, MailjetException {
    StringBuilder health = new StringBuilder();
    String htmlLineSeparator = "<br>";
    health.append(merriamWebsterHealth()).append(htmlLineSeparator);
    health.append(freeDictionaryHealth()).append(htmlLineSeparator);
    health.append(mongoHealth()).append(htmlLineSeparator);
    health.append(emailHealth()).append(htmlLineSeparator);
    return health.toString();
  }

  private String emailHealth() throws MailjetSocketTimeoutException, MailjetException {
    try {
      int statusCode = emailService.sendEmail("ping", "pong");
      if (statusCode == 200) {
        return "Email service is up. ";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "Email service is down";
  }

  private String mongoHealth() {
    try {
      mongoTemplate.getCollection("dictionary").countDocuments();
      //above line didn't throw error so mongo is up.
      return "Mongo is up.";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "Mongo is down.";
  }

  private String freeDictionaryHealth() {
    try {
      ResponseEntity<String> response = restTemplate.getForEntity(
          String.format(freeDictionaryEndpoint, "word"), String.class);
      if (response.getStatusCode() == HttpStatus.OK) {
        return "Free Dictionary is up. ";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "Free Dictionary is down";
  }

  private String merriamWebsterHealth() {
    try {
      ResponseEntity<String> response = restTemplate.getForEntity(
          String.format(merriamWebsterUrl, "word", merriamWebsterKey), String.class);
      if (response.getStatusCode() == HttpStatus.OK) {
        return "Merriam Webster Dictionary is up. ";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "Merriam Webster Dictionary is down";
  }

  @GetMapping("favicon.ico")
  void returnNoFavicon() {
  }

}
