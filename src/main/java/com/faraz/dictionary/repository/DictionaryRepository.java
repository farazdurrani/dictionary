package com.faraz.dictionary.repository;

import com.faraz.dictionary.entity.Dictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DictionaryRepository extends MongoRepository<Dictionary, String> {

  Optional<Dictionary> findByWord(String word);

}
