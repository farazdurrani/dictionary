package com.faraz.dictionary.entity;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class Test {
  @Id
  private String word;
  private Date lookupTime;

}
