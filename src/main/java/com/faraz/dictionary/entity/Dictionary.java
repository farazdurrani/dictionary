package com.faraz.dictionary.entity;

import org.springframework.data.annotation.Id;


public class Dictionary {

  @Id
  private String id;

  private String word;

  public Dictionary() {}

  public Dictionary(String word) {
    this.word = word;
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

}