package com.faraz.dictionary.entity;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.time.LocalTime;
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