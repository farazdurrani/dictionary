package com.faraz.dictionary.controller;

import com.faraz.dictionary.model.Word;
import com.faraz.dictionary.service.DictionaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DictionaryController {

  private final DictionaryService dictionaryService;

  public DictionaryController(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  @GetMapping("/")
  public String greetingForm(Model model) {
    model.addAttribute("word", new Word());
    return "index";
  }

  @PostMapping("/")
  public String greetingSubmit(@ModelAttribute Word word, Model model) {
    model.addAttribute("word", new Word());
    model.addAttribute("prevWord", word.getWord());
    model.addAttribute("definitions", dictionaryService.getDefinitions(word.getWord(), true));
    return "index";
  }
}
