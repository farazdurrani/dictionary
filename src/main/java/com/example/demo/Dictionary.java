package com.example.demo;

import org.springframework.data.annotation.Id;


public class Dictionary {

  @Id
  public String id;

  public String word;

  public Dictionary() {}

  public Dictionary(String word) {
    this.word = word;
  }

}