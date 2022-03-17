package com.example.demo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DictionaryRepository extends MongoRepository<Dictionary, String> {

  Dictionary findByWord(String word);

}
