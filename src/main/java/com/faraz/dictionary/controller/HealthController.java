package com.faraz.dictionary.controller;

import com.faraz.dictionary.service.EmailService;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
    ResponseEntity<String> response = restTemplate.getForEntity(
        String.format(merriamWebsterUrl, "word", merriamWebsterKey), String.class);
    String json = response.getBody();
    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
    StringBuilder health = new StringBuilder();
    if (!flattenJson.isEmpty()) {
      health.append("Merriam Webster Dictionary is up. ").append(System.lineSeparator());
    }
    mongoTemplate.getCollection("dictionary").countDocuments();
    //above line didn't throw error so mongo is up.
    health.append("Mongo is up.");
    return health.toString();
  }

  @GetMapping("favicon.ico")
  void returnNoFavicon() {
  }

}
