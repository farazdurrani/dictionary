package com.example.demo;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
public class DictionaryController {

  private final RestTemplate restTemplate;
  private final DictionaryRepository dictionaryRepository;
  private final String key;
  private final String url;

  public DictionaryController(RestTemplate restTemplate, DictionaryRepository dictionaryRepository, @Value("${dictionary.key}") String key,
                              @Value("${dictionary.url}") String url) {
    this.restTemplate = restTemplate;
    this.dictionaryRepository = dictionaryRepository;
    this.key = key;
    this.url = url;
  }

  @GetMapping("/{word}")
  public String get(@PathVariable String word, Model model) {
    this.dictionaryRepository.save(new Dictionary(word));
    ResponseEntity<String> response = restTemplate.getForEntity(String.format(url, word, key), String.class);
    String json = response.getBody();
    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
    flattenJson.keySet().removeIf(x -> !x.contains("shortdef"));
    model.addAttribute("definitions", flattenJson.values());
    return "index";
  }
}
