package com.faraz.dictionary.batch;

import com.faraz.dictionary.entity.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile("batch")
public class BatchProcessor {

  @Autowired
  private MongoTemplate mongoTemplate;

  @PostConstruct
  public void uploadWord() throws IOException {
    Set<String> lines = new HashSet<>(Files.readAllLines(Paths.get("words")));
    Date date = Date.from(Instant.now().minus(30, ChronoUnit.DAYS));
    List<Dictionary> dictionaryList = lines.stream().map(line -> new Dictionary(line, date)).collect(
        Collectors.toList());
    mongoTemplate.insert(dictionaryList, Dictionary.class);
  }
}
