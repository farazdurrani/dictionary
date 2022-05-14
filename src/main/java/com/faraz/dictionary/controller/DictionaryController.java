package com.faraz.dictionary.controller;

import com.faraz.dictionary.model.Word;
import com.faraz.dictionary.service.DictionaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class DictionaryController {

  private final DictionaryService dictionaryService;

  public DictionaryController(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  @GetMapping("/")
  public String handleGet(Model model) {
    model.addAttribute("word", new Word());
    return "index";
  }

  @PostMapping("/")
  public String handlePost(@ModelAttribute Word word, Model model) {
    model.addAttribute("word", new Word());
    String _word = word.getWord().trim().toLowerCase();
    model.addAttribute("prevWord", _word);
    List<String> definitions = dictionaryService.getDefinitions(_word);
    model.addAttribute("definitions", definitions);
    if (definitions.stream().anyMatch(def -> def.toLowerCase().contains("no definitions"))) {
      model.addAttribute("noDefinition", true);
    }
    return "index";
  }

  @GetMapping("/save/{word}")
  @ResponseBody
  public String save(@PathVariable String word) {
    return dictionaryService.save(word);
  }
}
