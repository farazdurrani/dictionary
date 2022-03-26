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

  public String getWord() {
    return this.word;
  }

  public void setLookupTime(Date lookupTime) {
    this.lookupTime = lookupTime;
  }
}