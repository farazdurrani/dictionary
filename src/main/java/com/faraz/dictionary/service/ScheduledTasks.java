package com.faraz.dictionary.service;

import com.faraz.dictionary.entity.Dictionary;
import com.faraz.dictionary.repository.DictionaryRepository;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ScheduledTasks {

  private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
  private final MongoTemplate mongoTemplate;
  private final EmailService emailService;
  private final DictionaryService dictionaryService;
  private final DictionaryRepository dictionaryRepository;
  private final int minimumWords;

  public ScheduledTasks(MongoTemplate mongoTemplate, EmailService emailService, DictionaryService dictionaryService,
                        DictionaryRepository dictionaryRepository, @Value("${minimum:25}") int mininumWords) {
    this.mongoTemplate = mongoTemplate;
    this.emailService = emailService;
    this.dictionaryService = dictionaryService;
    this.dictionaryRepository = dictionaryRepository;
    this.minimumWords = mininumWords;
  }

  @Scheduled(cron = "0 0 3 * * *", zone = "America/Chicago")
  public void everyDayTask() throws MailjetSocketTimeoutException, MailjetException {
    logger.info("Started 24 hour task");
    Instant now = Instant.now();
    Instant prev = now.minus(1, ChronoUnit.DAYS);
    List<Dictionary> words = wordsFromLast(now, prev);
    List<String> definitions = getDefinitions(words);
    String subject = "Words lookup in the past 24 hours";
    sendEmail(definitions, subject);
    logger.info("Finished 24 hour task");
  }

  @Scheduled(cron = "0 0 3 * * *", zone = "America/Chicago")
  public void sendRandomDefinitions() throws MailjetSocketTimeoutException, MailjetException {
    Instant now = Instant.now();
    Instant prev = now.minus(1, ChronoUnit.DAYS);
    List<Dictionary> words = wordsFromLast(now, prev);
    if (words.size() >= minimumWords) {
      logger.info("Enough words lookup in the past 24 hours. Not sending random definitions.");
      return;
    }
    int wordLimit = minimumWords - words.size();
    logger.info("Started random definitions. wordLimit {}", wordLimit);
    Aggregation aggregation = createAggregationQuery(wordLimit);
    words = mongoTemplate.aggregate(aggregation, mongoTemplate.getCollectionName(Dictionary.class),
        Dictionary.class).getMappedResults();
    if (words.isEmpty()) {
      words = setReminded().stream().limit(wordLimit).collect(Collectors.toList());
    }
    List<String> definitions = getDefinitions(words);
    String subjectLine = "Random Definitions for the Day!";
    sendEmail(definitions, subjectLine);
    words = words.stream().map(w -> setReminded(w, true)).collect(Collectors.toList());
    dictionaryRepository.saveAll(words);
    logger.info("Finished sending random definitions");
  }

  @Scheduled(cron = "0 0 9 * * *", zone = "America/Chicago")
  public void backup() throws MailjetSocketTimeoutException, MailjetException {
    logger.info("Backup started");
    List<String> definitions = dictionaryRepository.findAll(Sort.by(Sort.Direction.DESC, "lookupTime")).stream().map(
        Dictionary::getWord).distinct().map(this::anchor).collect(Collectors.toList());
    definitions.add(0, "Count: " + definitions.size());
    String subject = "Words Backup";
    sendEmail(definitions, subject);
    logger.info("Backup ended");
  }

  private List<Dictionary> setReminded() {
    Query query = new Query().addCriteria(Criteria.where("lookupTime").lt(Date.from(Instant.now().minus(7, ChronoUnit.DAYS))));
    List<Dictionary> allWords = mongoTemplate.find(query, Dictionary.class).stream().map(w -> setReminded(w, false)).collect(
        Collectors.toList());
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
        word -> massageDefinition(word, count.getAndIncrement())).flatMap(List::stream).collect(Collectors.toList());
  }

  private void sendEmail(List<String> definitions, String subject) throws MailjetSocketTimeoutException, MailjetException {
    String body = String.join("<br>", definitions);
    body = "<div style=\"font-size:20px\">" + body + "</div>";
    int status = emailService.sendEmail(subject, body);
    logger.info("Sent definitions with status " + status);
  }

  private List<String> massageDefinition(String word, int counter) {
    List<String> meanings = dictionaryService.getDefinitionsV2(word).stream().limit(6).collect(Collectors.toList());
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
    return "<a href='https://www.google.com/search?q=define: " + word + "' target='_blank'>" + StringUtils.capitalize(
        word) + "</a>";
  }

  private Aggregation createAggregationQuery(int wordLimit) {
    MatchOperation matchStage = Aggregation.match(new Criteria().andOperator(Criteria.where("reminded").is(false),
        Criteria.where("lookupTime").lt(Date.from(Instant.now().minus(7, ChronoUnit.DAYS)))));
    SampleOperation sampleOperation = Aggregation.sample(wordLimit);
    return Aggregation.newAggregation(matchStage, sampleOperation);
  }

  /**
   * Db call
   */
  private List<Dictionary> wordsFromLast(Instant now, Instant prev) {
    Date startDate = Date.from(now);
    Date endDate = Date.from(prev);
    Query query = new Query();
    query.addCriteria(Criteria.where("lookupTime").gte(endDate).lt(startDate));
    return new ArrayList<>(mongoTemplate.find(query, Dictionary.class));
  }
}
