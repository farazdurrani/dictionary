package com.faraz.dictionary.service;

import com.faraz.dictionary.entity.Dictionary;
import com.faraz.dictionary.repository.DictionaryRepository;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class DictionaryService {
  private final RestTemplate restTemplate;
  private final DictionaryRepository dictionaryRepository;
  private final String merriamWebsterKey;
  private final String merriamWebsterUrl;
  private final String freeDictionaryEndpoint;

  public DictionaryService(RestTemplate restTemplate, DictionaryRepository dictionaryRepository,
                           @Value("${dictionary.merriamWebster.key}") String merriamWebsterKey,
                           @Value("${dictionary.merriamWebster.url}") String merriamWebsterUrl,
                           @Value("${dictionary.freeDictionary.url}") String freeDictionaryEndpoint) {
    this.restTemplate = restTemplate;
    this.dictionaryRepository = dictionaryRepository;
    this.merriamWebsterKey = merriamWebsterKey;
    this.merriamWebsterUrl = merriamWebsterUrl;
    this.freeDictionaryEndpoint = freeDictionaryEndpoint;
  }

  public Collection<?> getDefinitions(String word) {
    ResponseEntity<String> response = restTemplate.getForEntity(String.format(merriamWebsterUrl, word, merriamWebsterKey), String.class);
    String json = response.getBody();
    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
    List<Object> orig = new ArrayList<>(flattenJson.values());
    flattenJson.keySet().removeIf(x -> !x.contains("shortdef"));
    if (flattenJson.values().isEmpty()) {
      orig.set(0, "No definition found for " + word + ". Perhaps, you meant:");
      return orig;
    }
    this.dictionaryRepository.save(new Dictionary(word));
    return flattenJson.values();
  }
}
