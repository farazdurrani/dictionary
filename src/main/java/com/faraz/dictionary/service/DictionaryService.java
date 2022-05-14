package com.faraz.dictionary.service;

import com.faraz.dictionary.entity.Dictionary;
import com.faraz.dictionary.repository.DictionaryRepository;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class DictionaryService {
  private static final String NO_DEFINITION_FOUND = "No definitions found for ";
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

  public List<String> getDefinitions(String word) {
    if (dictionaryRepository.findByWord(word).isPresent()) {
      return List.of("Already Looked-up");
    } else {
      List<String> definitions = getDefinitionsV2(word);
      save(word, definitions);
      return definitions;
    }
  }

  public String save(String word) {
    if (dictionaryRepository.findByWord(word).isEmpty()) {
      Dictionary dictionaryInstance = new Dictionary(word, new Date(), false);
      dictionaryRepository.save(dictionaryInstance);
      return word + " saved";
    } else {
      return word + " already in there";
    }
  }

  public List<String> getDefinitionsV2(String word) {
    List<String> definitions = merriamWebsterDefinitions(word);
    definitions.addAll(freeDictionaryDefinitions(word));
    return definitions;
  }

  private void save(String word, List<String> definitions) {
    if (!definitions.isEmpty() && !definitions.get(0).contains(NO_DEFINITION_FOUND)) {
      Dictionary dictionaryInstance = new Dictionary(word, new Date(), false);
      dictionaryRepository.save(dictionaryInstance);
    }
  }

  private List<String> merriamWebsterDefinitions(String word) {
    String json = restTemplate.getForEntity(String.format(merriamWebsterUrl, word, merriamWebsterKey),
        String.class).getBody();
    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
    List<Object> orig = new ArrayList<>(flattenJson.values());
    flattenJson.keySet().removeIf(x -> !x.contains("shortdef"));
    if (flattenJson.values().isEmpty()) {
      orig.add(0, NO_DEFINITION_FOUND + word + ". Perhaps, you meant:");
      return orig.stream().filter(String.class::isInstance).map(String.class::cast).collect(
          Collectors.toList());
    }
    return flattenJson.values().stream().filter(String.class::isInstance).map(String.class::cast).collect(
        Collectors.toList());
  }

  private List<String> freeDictionaryDefinitions(String word) {
    try {
      String json = restTemplate.getForEntity(String.format(freeDictionaryEndpoint, word),
          String.class).getBody();
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
    } catch (Exception e) {
      return Arrays.asList(NO_DEFINITION_FOUND + word + " from TheFreeDictionary");
    }
  }

  private Predicate<? super String> contains(String search) {
    return key -> key.split("\\.")[key.split("\\.").length - 1].contains(search);
  }
}
