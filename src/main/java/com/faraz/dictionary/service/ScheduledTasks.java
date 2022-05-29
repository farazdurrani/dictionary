package com.faraz.dictionary.service;

import com.faraz.dictionary.entity.Dictionary;
import com.faraz.dictionary.repository.DictionaryRepository;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.of;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.format.TextStyle.FULL;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Locale.US;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sample;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ScheduledTasks {

  private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
  private static final String CHICAGO = "America/Chicago";
  private static final ZoneId CHICAGO_TIME = of(CHICAGO);

  private final MongoTemplate mongoTemplate;
  private final EmailService emailService;
  private final DictionaryService dictionaryService;
  private final DictionaryRepository dictionaryRepository;
  private final int minimumWords;

  public ScheduledTasks(MongoTemplate mongoTemplate, EmailService emailService, DictionaryService dictionaryService,
                        DictionaryRepository dictionaryRepository, @Value("${minimum:25}") int minimumWords) {
    this.mongoTemplate = mongoTemplate;
    this.emailService = emailService;
    this.dictionaryService = dictionaryService;
    this.dictionaryRepository = dictionaryRepository;
    this.minimumWords = minimumWords;
  }

  @Scheduled(cron = "0 0 3 * * *", zone = CHICAGO)
  public void everyDayTask() throws MailjetSocketTimeoutException, MailjetException {
    logger.info("Started 24 hour task");
    LocalDateTime startDate = now(CHICAGO_TIME).minus(1, DAYS);
    LocalDateTime endDate = now(CHICAGO_TIME).minus(2, DAYS);
    List<Dictionary> words = wordsFromLast(startDate, endDate);
    if (!words.isEmpty()) {
      List<String> definitions = getDefinitions(words);
      String subject = formSubjectLine(startDate, endDate);
      sendEmail(definitions, subject);
    }
    logger.info("Finished 24 hour task");
  }

  @Scheduled(cron = "0 0 3 * * *", zone = CHICAGO)
  public void sendRandomDefinitions() throws MailjetSocketTimeoutException, MailjetException {
    LocalDateTime startDate = now(CHICAGO_TIME).minus(1, DAYS);
    LocalDateTime endDate = now(CHICAGO_TIME).minus(2, DAYS);
    List<Dictionary> words = wordsFromLast(startDate, endDate);
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
      words = setReminded().stream().limit(wordLimit).collect(toList());
    }
    List<String> definitions = getDefinitions(words);
    String subjectLine = "Random Definitions for the Day!";
    sendEmail(definitions, subjectLine);
    words = words.stream().map(w -> setReminded(w, true)).collect(toList());
    dictionaryRepository.saveAll(words);
    logger.info("Finished sending random definitions");
  }

  @Scheduled(cron = "0 0 9 * * *", zone = CHICAGO)
  public void backup() throws MailjetSocketTimeoutException, MailjetException {
    logger.info("Backup started");
    List<String> definitions = dictionaryRepository.findAll(by(DESC, "lookupTime")).stream().map(
        Dictionary::getWord).distinct().map(this::anchor).collect(toList());
    definitions.add(0, "Count: " + definitions.size());
    String subject = "Words Backup";
    sendEmail(definitions, subject);
    logger.info("Backup ended");
  }

  private List<Dictionary> setReminded() {
    Query query = new Query().addCriteria(where("lookupTime").lt(now(CHICAGO_TIME).minus(7, DAYS)));
    List<Dictionary> allWords = mongoTemplate.find(query, Dictionary.class).stream().map(w -> setReminded(w, false)).collect(
        toList());
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
        word -> massageDefinition(word, count.getAndIncrement())).flatMap(List::stream).collect(toList());
  }

  private void sendEmail(List<String> definitions, String subject) throws MailjetSocketTimeoutException, MailjetException {
    String body = join("<br>", definitions);
    body = "<div style=\"font-size:20px\">" + body + "</div>";
    int status = emailService.sendEmail(subject, body);
    logger.info("Sent definitions with status " + status);
  }

  private List<String> massageDefinition(String word, int counter) {
    List<String> meanings = dictionaryService.getDefinitionsV2(word).stream().limit(6).collect(toList());
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
    return "<a href='https://www.google.com/search?q=define: " + word + "' target='_blank'>" + capitalize(word) + "</a>";
  }

  private Aggregation createAggregationQuery(int wordLimit) {
    MatchOperation matchStage = match(new Criteria().andOperator(where("reminded").is(false),
        where("lookupTime").lt(now(CHICAGO_TIME).minus(8, DAYS))));
    SampleOperation sampleOperation = sample(wordLimit);
    return newAggregation(matchStage, sampleOperation);
  }

  private static String formSubjectLine(LocalDateTime startDate, LocalDateTime endDate) {
    String startDay = startDate.getDayOfMonth() + suffix(startDate.getDayOfMonth());
    String endDay = endDate.getDayOfMonth() + suffix(endDate.getDayOfMonth());
    String startTime = startDate.format(ofPattern("ha"));
    String endTime = endDate.format(ofPattern("ha"));
    String startMonthName = startDate.getMonth().getDisplayName(FULL, US);
    String endMonthName = endDate.getMonth().getDisplayName(FULL, US);
    return format("Words looked-up between %s of %s-%s and %s of %s-%s", endTime, endMonthName, endDay, startTime,
        startMonthName, startDay);
  }

  private static String suffix(final int n) {
    if (n >= 11 && n <= 13) {
      return "th";
    }
    switch (n % 10) {
      case 1:
        return "st";
      case 2:
        return "nd";
      case 3:
        return "rd";
      default:
        return "th";
    }
  }

  /**
   * Db call
   */
  private List<Dictionary> wordsFromLast(LocalDateTime startDate, LocalDateTime endDate) {
    Query query = new Query().addCriteria(where("lookupTime").gte(endDate).lt(startDate));
    return new ArrayList<>(mongoTemplate.find(query, Dictionary.class));
  }
}
