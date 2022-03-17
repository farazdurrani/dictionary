package com.example.demo;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

@RestController
public class HealthController {

  private final String url;
  private final String key;
  private final MongoTemplate mongoTemplate;
  private final RestTemplate restTemplate;

  public HealthController(@Value("${dictionaryurl}") String url, @Value("${dictionarykey}") String key, MongoTemplate mongoTemplate, RestTemplate restTemplate) {
    this.url = url;
    this.key = key;
    this.mongoTemplate = mongoTemplate;
    this.restTemplate = restTemplate;
  }

  @GetMapping("/health")
  public String health() {
    ResponseEntity<String> response = restTemplate.getForEntity(String.format(url, "word", key), String.class);
    String json = response.getBody();
    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
    StringBuilder health = new StringBuilder();
    if (!flattenJson.isEmpty()) {
      health.append("Dictionary is up. ").append(System.lineSeparator());
    }
    long docCount = mongoTemplate.getCollection("dictionary").countDocuments();
    if (docCount > 0) {
      health.append("Mongo is up.");
    }
    return health.toString();
  }

  @GetMapping("favicon.ico")
  void returnNoFavicon() {
  }

}
