package com.faraz.dictionary.batch;

import com.faraz.dictionary.entity.Dictionary;
import com.faraz.dictionary.repository.DictionaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Component
@Profile("reminder")
public class LoadReminder {

  private static final Logger logger = LoggerFactory.getLogger(LoadReminder.class);

  @Autowired
  private DictionaryRepository dictionaryRepository;

  @PostConstruct
  public void loadReminders() throws IOException {
    logger.info("Started loading reminders all at once");
    List<Dictionary> words = dictionaryRepository.findAll();
    dictionaryRepository.saveAll(words);
    logger.info("Finished loading reminders all at once");
  }
}
