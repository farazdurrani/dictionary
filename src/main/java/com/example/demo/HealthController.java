package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  private final String url;

  public HealthController(@Value("${url}") String url) {
    this.url = url;
  }

  @GetMapping("/health")
  public String health(){
    System.out.println("URL is " +  url);
    return "App is up " + url;
  }

}
