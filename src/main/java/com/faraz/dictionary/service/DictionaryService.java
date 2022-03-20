package com.faraz.dictionary.service;

import com.faraz.dictionary.entity.Dictionary;
import com.faraz.dictionary.repository.DictionaryRepository;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

  public List<String> getDefinitions(String word, boolean save) {
    List<String> definitions = merriamWebsterDefinitions(word);
    save(definitions, word, save);
    List<String> freeDictionaryDefinitions = freeDictionaryDefinitions(word);
    save(freeDictionaryDefinitions, word, save);
    definitions.addAll(freeDictionaryDefinitions);
    addHeaderAndFooter(definitions, word);
    return definitions;
  }

  private void addHeaderAndFooter(List<String> definitions, String word) {
    definitions.add(0, "Definition of " + word.toUpperCase());
    definitions.add(System.lineSeparator());
    definitions.add(System.lineSeparator());
  }

  private void save(List<String> definitions, String word, boolean save) {
    if (!definitions.get(0).contains("No definitions found for ") && save) {
      Optional<Dictionary> exists = this.dictionaryRepository.findByWord(word);
      if (exists.isPresent()) {
        this.dictionaryRepository.delete(exists.get());
      }
      this.dictionaryRepository.save(new Dictionary(word, new Date()));
    }
  }

  private List<String> merriamWebsterDefinitions(String word) {
    String json = restTemplate.getForEntity(String.format(merriamWebsterUrl, word, merriamWebsterKey),
        String.class).getBody();
    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
    List<Object> orig = new ArrayList<>(flattenJson.values());
    flattenJson.keySet().removeIf(x -> !x.contains("shortdef"));
    if (flattenJson.values().isEmpty()) {
      orig.add(0, "No definitions found for " + word + ". Perhaps, you meant:");
      return orig.stream().map(String.class::cast).collect(Collectors.toList());
    }
    return flattenJson.values().stream().map(String.class::cast).collect(Collectors.toList());
  }

  private List<String> freeDictionaryDefinitions(String word) {
    String json = null;
    try {
      json = restTemplate.getForEntity(String.format(freeDictionaryEndpoint, word), String.class).getBody();
    } catch (Exception e) {
      List<String> notFound = new ArrayList<>();
      notFound.add("No definitions found for " + word + " from TheFreeDictionary");
      return notFound;
    }
    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
    List<String> definitions = flattenJson.keySet().stream().filter(contains("definition")).map(
        flattenJson::get).map(String.class::cast).collect(Collectors.toList());
    List<String> synonyms = flattenJson.keySet().stream().filter(contains("synonyms")).filter(
        key -> flattenJson.get(key) instanceof String && !(flattenJson.get(key)).toString().isEmpty()).map(
        flattenJson::get).map(String.class::cast).collect(Collectors.toList());
    List<String> examples = flattenJson.keySet().stream().filter(contains("example")).map(
        flattenJson::get).map(String.class::cast).collect(Collectors.toList());
    List<String> combined = new ArrayList<>();
    combined.addAll(definitions);
    combined.add("synonyms:".toUpperCase());
    combined.addAll(synonyms);
    combined.add("examples:".toUpperCase());
    combined.addAll(examples);
    return combined;
  }

  private Predicate<? super String> contains(String search) {
    return key -> key.split("\\.")[key.split("\\.").length - 1].contains(search);
  }
}
