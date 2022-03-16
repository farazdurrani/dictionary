package com.example.demo;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
    if(false)
      SpringApplication.exit(context, () -> 1);
  }

  @Override
  public void run(String... args) throws Exception {
    String test = "https://dictionaryapi.com/api/v3/references/collegiate/json/test?key=c3d51672-3f28-4615-83d7-475859de6989";
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

}
