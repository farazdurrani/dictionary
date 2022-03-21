package com.faraz.dictionary.controller;

import com.faraz.dictionary.service.DictionaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DictionaryController {

  private final DictionaryService dictionaryService;

  public DictionaryController(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  @GetMapping("/{word}")
  public String get(@PathVariable String word, Model model) {
    model.addAttribute("definitions", dictionaryService.getDefinitions(word, true));
    return "index";
  }
}
