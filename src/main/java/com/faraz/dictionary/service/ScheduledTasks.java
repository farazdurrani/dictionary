package com.faraz.dictionary.service;

import com.faraz.dictionary.entity.Dictionary;
import com.faraz.dictionary.repository.DictionaryRepository;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ScheduledTasks {

  private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
  private static final int MINIMUM_WORDS = 50;
  private final MongoTemplate mongoTemplate;
  private final EmailService emailService;
  private final DictionaryService dictionaryService;
  private final DictionaryRepository dictionaryRepository;

  public ScheduledTasks(MongoTemplate mongoTemplate, EmailService emailService,
                        DictionaryService dictionaryService, DictionaryRepository dictionaryRepository) {
    this.mongoTemplate = mongoTemplate;
    this.emailService = emailService;
    this.dictionaryService = dictionaryService;
    this.dictionaryRepository = dictionaryRepository;
  }

  @Scheduled(fixedRate = 600000000L)
  public void delete() throws MailjetSocketTimeoutException, MailjetException {
    logger.info("Started 24 hour task");
    Instant now = Instant.now();
    Instant prev = now.minus(1, ChronoUnit.DAYS);
    Collection<Dictionary> words = wordsFromLast(now, prev).stream().limit(2).collect(
        Collectors.toList());
    List<String> definitions = getDefinitions(words);
    String subject = "Words lookup in the past 24 hours";
    sendEmail(definitions, subject);
    if (words.size() < MINIMUM_WORDS) {
      System.out.println();
//      sendRandomDefinitions(MINIMUM_WORDS - definitions.size());
    }
    logger.info("Finished 24 hour task");
  }

  @Scheduled(cron = "0 0 3 * * *", zone = "America/Chicago")
  public void everyDayTask() throws MailjetSocketTimeoutException, MailjetException {
    logger.info("Started 24 hour task");
    Instant now = Instant.now();
    Instant prev = now.minus(1, ChronoUnit.DAYS);
    Collection<Dictionary> words = wordsFromLast(now, prev);
    List<String> definitions = getDefinitions(words);
    String subject = "Words lookup in the past 24 hours";
    sendEmail(definitions, subject);
    if (words.size() < MINIMUM_WORDS) {
      sendRandomDefinitions(MINIMUM_WORDS - definitions.size());
    }
    logger.info("Finished 24 hour task");
  }

  @Scheduled(cron = "0 0 4 * * SUN", zone = "America/Chicago")
  public void sendRandomDefinitions() throws MailjetSocketTimeoutException, MailjetException {
    sendRandomDefinitions(MINIMUM_WORDS);
  }

  private List<String> sendRandomDefinitions(
      int wordLimit) throws MailjetSocketTimeoutException, MailjetException {
    logger.info("Started random definitions");
    Query query = new Query();
    query.addCriteria(Criteria.where("reminded").is(Boolean.valueOf(false))).limit(wordLimit);
    List<Dictionary> words = mongoTemplate.find(query, Dictionary.class);
    if (words.isEmpty()) {
      words = setReminded().stream().limit(wordLimit).collect(Collectors.toList());
    }
    List<String> definitions = getDefinitions(words);
    sendEmail(definitions, "Random definitions of the week!");
    words = words.stream().map(w -> setReminded(w, true)).collect(Collectors.toList());
    dictionaryRepository.saveAll(words);
    logger.info("Finished sending random definitions");
    return words.stream().map(Dictionary::getWord).collect(Collectors.toList());
  }

  @Scheduled(cron = "0 0 9 * * *", zone = "America/Chicago")
  public void backup() throws MailjetSocketTimeoutException, MailjetException {
    logger.info("Backup started");
    List<String> definitions = dictionaryRepository.findAll().stream().map(
        Dictionary::getWord).distinct().collect(Collectors.toList());
    definitions.add(0, "Count: " + definitions.size());
    String subject = "Words Backup";
    sendEmail(definitions, subject);
    logger.info("Backup ended");
  }

  private List<Dictionary> setReminded() {
    List<Dictionary> allWords = mongoTemplate.findAll(Dictionary.class).stream().map(
        w -> setReminded(w, false)).collect(Collectors.toList());
    dictionaryRepository.saveAll(allWords);
    return allWords;
  }

  private Dictionary setReminded(Dictionary dictionary, boolean reminded) {
    dictionary.setReminded(reminded);
    return dictionary;
  }

  private List<String> getDefinitions(Collection<Dictionary> words) {
    AtomicInteger count = new AtomicInteger(1);
    return words.stream().map(Dictionary::getWord).distinct().map(
        word -> massageDefinition(word, count.getAndIncrement())).flatMap(List::stream).collect(
        Collectors.toList());
  }

  private void sendEmail(List<String> definitions,
                         String subject) throws MailjetSocketTimeoutException, MailjetException {
    String body = String.join("<br>", definitions);
    body = "<div style=\"font-size:20px\">" + body + "</div>";
    int status = emailService.sendEmail(subject, body);
    logger.info("Sent definitions with status " + status);
  }

  private List<String> massageDefinition(String word, int counter) {
    List<String> meanings = dictionaryService.getDefinitions(word, false).stream().limit(6).collect(
        Collectors.toList());
    for (int i = 0; i < meanings.size(); i++) {
      if (i == 0) {
        meanings.add(i, counter + "- Definition of " + anchor(word));
        continue;
      }
      meanings.set(i, "- ".concat(meanings.get(i)));
    }
    meanings.add(System.lineSeparator());
    return meanings;
  }

  private String anchor(String word) {
    String a =
        "<a href=\'https://www.google.com/search?q=define: " + word.toLowerCase() + "\' target=\'_blank\'>" + word.toUpperCase() +
            "</a>";
    return word;
  }

  /**
   * Db call
   */
  public List<Dictionary> wordsFromLast(Instant now, Instant prev) {
    Date startDate = Date.from(now);
    Date endDate = Date.from(prev);
    Query query = new Query();
    query.addCriteria(Criteria.where("lookupTime").gte(endDate).lt(startDate));
    return mongoTemplate.find(query, Dictionary.class);
  }
}
