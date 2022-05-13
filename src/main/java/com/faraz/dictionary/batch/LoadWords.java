package com.faraz.dictionary.batch;

import com.faraz.dictionary.entity.Dictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.stream.IntStream.rangeClosed;

@Component
@Profile("batch")
public class LoadWords {

  private static final Logger logger = LoggerFactory.getLogger(LoadWords.class);

  @Autowired
  private MongoTemplate mongoTemplate;

  @PostConstruct
  public void uploadWord() throws IOException {
    logger.info("Start of uploadWord ");
    //TODO beware of running remove method below. It deletes all documents.
    //mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(), Dictionary.class);
    List<Date> dates = date(2256); //TODO do you need that many dates?
    List<Dictionary> dictionaryList = Files.readAllLines(Paths.get("words")).stream().map(String::trim)
        .map(String::toLowerCase).distinct().map(line -> new Dictionary(line, dates.remove(dates.size() - 1), false))
        .collect(Collectors.toList());
    mongoTemplate.insert(dictionaryList, Dictionary.class);
    logger.info("End of uploadWord ");
  }

  private List<Date> date(int size) {
    Instant instant = Clock.systemUTC().instant().minus(30, DAYS);
    List<Date> dates = new ArrayList<>();
    dates.add(Date.from(instant.minus(1, MINUTES)));
    rangeClosed(1, size).forEach(i -> dates.add(Date.from(dates.get(dates.size() - 1).toInstant().minus(1, MINUTES))));
    return dates;
  }
}
