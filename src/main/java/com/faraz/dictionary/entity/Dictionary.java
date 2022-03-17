package com.faraz.dictionary.entity;

import org.springframework.data.annotation.Id;

import java.util.List;


public class Dictionary {

  @Id
  private String id;

  private String word;
  private List<String> meaning;

  public Dictionary() {}

  public Dictionary(String word, List<String> meaning) {
    this.word = word;
    this.meaning = meaning;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }


  public List<String> getMeaning() {
    return meaning;
  }

  public void setMeaning(List<String> meaning) {
    this.meaning = meaning;
  }
}