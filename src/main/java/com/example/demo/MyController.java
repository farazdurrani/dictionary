package com.example.demo;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
public class MyController {

  private static final String KEY = "c3d51672-3f28-4615-83d7-475859de6989";
  private static final String URL = "https://www.dictionaryapi.com/api/v3/references/collegiate/json/%s?key=%s";

  @Autowired
  private RestTemplate restTemplate;

  @GetMapping("/{word}")
  public String get(@PathVariable String word, Model model) {
    ResponseEntity<String> response = restTemplate.getForEntity(String.format(URL, word, KEY), String.class);
    String json = response.getBody();
    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
    flattenJson.keySet().removeIf(x -> !x.contains("shortdef"));
    model.addAttribute("definitions", flattenJson.values());
    return "index";
  }
}
