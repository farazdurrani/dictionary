package com.faraz.dictionary.service;

import com.faraz.dictionary.entity.Dictionary;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduledTasks {

  private final MongoTemplate mongoTemplate;
  private final EmailService emailService;
  private final DictionaryService dictionaryService;

  public ScheduledTasks(MongoTemplate mongoTemplate, EmailService emailService,
                        DictionaryService dictionaryService) {
    this.mongoTemplate = mongoTemplate;
    this.emailService = emailService;
    this.dictionaryService = dictionaryService;
  }

  @Scheduled(cron = "0 0 1 * * *", zone = "America/Chicago")
  public void everyDayTask() throws MailjetSocketTimeoutException, MailjetException {
    Instant now = Instant.now();
    Instant prev = now.minus(1, ChronoUnit.DAYS);
    List<Dictionary> words = wordsFromLast(now, prev);
    sendEmail(words, "24 hours");
  }

  @Scheduled(cron = "0 0 1 * * SUN", zone = "America/Chicago")
  public void everyWeekTask() throws MailjetSocketTimeoutException, MailjetException {
    Instant now = Instant.now();
    Instant prev = now.minus(1, ChronoUnit.WEEKS);
    List<Dictionary> words = wordsFromLast(now, prev);
    sendEmail(words, "7 days");
  }

  private void sendEmail(List<Dictionary> words,
                         String time) throws MailjetSocketTimeoutException, MailjetException {
    List<String> definitions = null;
    if (!words.isEmpty()) {
      definitions = words.stream().map(Dictionary::getWord).map(
          word -> dictionaryService.getDefinitions(word, false).stream().limit(6).collect(
              Collectors.toList())).flatMap(List::stream).collect(Collectors.toList());
    } else {
      definitions = Arrays.asList("No words lookup in the past " + time);
    }
    String body = String.join("<br>", definitions);
    body = "<div style=\"font-size:25px\">" + body + "</div>";
    emailService.sendEmail("Words lookup in the past " + time, body);
  }

  public List<Dictionary> wordsFromLast(Instant now, Instant prev) {
    Date startDate = Date.from(now);
    Date endDate = Date.from(prev);
    Query query = new Query();
    query.addCriteria(Criteria.where("lookupTime").gte(endDate).lt(startDate));
    return mongoTemplate.find(query, Dictionary.class);
  }
}
