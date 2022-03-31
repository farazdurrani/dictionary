package com.faraz.dictionary.entity;

import org.springframework.data.annotation.Id;

import java.util.Date;


public class Dictionary {

  @Id
  private String id;
  private String word;
  private Date lookupTime;
  private boolean reminded;

  public Dictionary() {}

  public Dictionary(String word, Date lookupTime, boolean reminded) {
    this.word = word;
    this.lookupTime = lookupTime;
    this.reminded = reminded;
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

  public Date getLookupTime() {
    return lookupTime;
  }

  public void setLookupTime(Date lookupTime) {
    this.lookupTime = lookupTime;
  }

  public boolean isReminded() {
    return reminded;
  }

  public void setReminded(boolean reminded) {
    this.reminded = reminded;
  }
}