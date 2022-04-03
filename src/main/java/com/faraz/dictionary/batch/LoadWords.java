package com.faraz.dictionary.batch;

import com.faraz.dictionary.entity.Dictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("batch")
public class LoadWords {

  private static final Logger logger = LoggerFactory.getLogger(LoadWords.class);

  @Autowired
  private MongoTemplate mongoTemplate;

  @PostConstruct
  public void uploadWord() throws IOException {
    logger.info("Start of uploadWord ");
    mongoTemplate.remove(new Query(), Dictionary.class);
    Date date = Date.from(Instant.now().minus(30, ChronoUnit.DAYS));
    List<Dictionary> dictionaryList = Files.readAllLines(Paths.get("words")).stream().map(String::trim).map(
        String::toLowerCase).distinct().map(line -> new Dictionary(line, date, false)).collect(
        Collectors.toList());
    mongoTemplate.insert(dictionaryList, Dictionary.class);
    logger.info("End of uploadWord ");
  }
}
