package com.faraz.dictionary.entity;

import org.springframework.data.annotation.Id;

import java.util.Date;


public class Dictionary {

  @Id
  private String id;

  private String word;
  private Date lookupTime;

  public Dictionary() {}

  public Dictionary(String word, Date lookupTime) {
    this.word = word;
    this.lookupTime = lookupTime;
  }

  public String getWord() {
    return this.word;
  }
}