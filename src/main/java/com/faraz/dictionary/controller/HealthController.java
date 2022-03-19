package com.faraz.dictionary.controller;

import com.github.wnameless.json.flattener.JsonFlattener;
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

  public HealthController(RestTemplate restTemplate, MongoTemplate mongoTemplate,
                          @Value("${dictionary.merriamWebster.key}") String merriamWebsterKey,
                          @Value("${dictionary.merriamWebster.url}") String merriamWebsterUrl,
                          @Value("${dictionary.freeDictionary.url}") String freeDictionaryEndpoint) {
    this.restTemplate = restTemplate;
    this.mongoTemplate = mongoTemplate;
    this.merriamWebsterKey = merriamWebsterKey;
    this.merriamWebsterUrl = merriamWebsterUrl;
    this.freeDictionaryEndpoint = freeDictionaryEndpoint;
  }

  //todo work on health
  //check status after making calls to dictionaries and thats enough
  //append thefreedictionary check and result
  @GetMapping({"/", "/health"})
  public String health() {
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
