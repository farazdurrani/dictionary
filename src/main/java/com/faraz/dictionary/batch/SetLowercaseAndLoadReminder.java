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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
//@Profile("reminder")
public class SetLowercaseAndLoadReminder {

  private static final Logger logger = LoggerFactory.getLogger(SetLowercaseAndLoadReminder.class);

  @Autowired
  private DictionaryRepository dictionaryRepository;

  @PostConstruct
  public void saveUniqueAndSetReminders() throws IOException {
//    dictionaryRepository.saveAll(Arrays.asList(new Dictionary()));
    List<Dictionary> words = dictionaryRepository.findAll();
    logger.info("Words loaded: {}", words.size());
    Map<String, Dictionary> map = new LinkedHashMap<>();
    for (Dictionary word : words) {
      Dictionary _word = new Dictionary();
      _word.setId(word.getId());
      _word.setWord(word.getWord().trim().toLowerCase());
      _word.setLookupTime(word.getLookupTime());
      _word.setReminded(false);
      map.put(word.getWord().trim().toLowerCase(), _word);
    }
    List<Dictionary> saved = dictionaryRepository.saveAll(map.values());
    logger.info("Words transformed: {}", map.values().size());
    logger.info("Finished loading reminders all at once");
  }
}
