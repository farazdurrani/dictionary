package com.faraz.dictionary.controller;

import com.faraz.dictionary.repository.DictionaryRepository;
import com.faraz.dictionary.entity.Dictionary;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class DictionaryController {

  private final RestTemplate restTemplate;
  private final DictionaryRepository dictionaryRepository;
  private final String merriamWebsterKey;
  private final String merriamWebsterUrl;
  private final String freeDictionaryEndpoint;

  public DictionaryController(RestTemplate restTemplate, DictionaryRepository dictionaryRepository,
                              @Value("${dictionary.merriamWebster.key}") String merriamWebsterKey,
                              @Value("${dictionary.merriamWebster.url}") String merriamWebsterUrl,
                              @Value("${dictionary.freeDictionary.url}") String freeDictionaryEndpoint) {
    this.restTemplate = restTemplate;
    this.dictionaryRepository = dictionaryRepository;
    this.merriamWebsterKey = merriamWebsterKey;
    this.merriamWebsterUrl = merriamWebsterUrl;
    this.freeDictionaryEndpoint = freeDictionaryEndpoint;
  }

  @GetMapping("/{word}")
  public String get(@PathVariable String word, Model model) {
    Optional<Dictionary> meaning = this.dictionaryRepository.findByWord(word);
    if (meaning.isPresent()) {
      model.addAttribute("definitions", meaning.get().getMeaning());
      return "index";
    }
    ResponseEntity<String> response = restTemplate.getForEntity(String.format(merriamWebsterUrl, word, merriamWebsterKey), String.class);
    String json = response.getBody();
    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
    List<Object> orig = new ArrayList<>(flattenJson.values());
    flattenJson.keySet().removeIf(x -> !x.contains("shortdef"));
    if (flattenJson.values().isEmpty()) {
      model.addAttribute("message", "No definition found for " + word + ". Perhaps, you meant:");
      model.addAttribute("definitions", orig);
      return "index";
    }
    this.dictionaryRepository.save(new Dictionary(word, new ArrayList(flattenJson.values())));
    model.addAttribute("definitions", flattenJson.values());
    return "index";
  }
}
