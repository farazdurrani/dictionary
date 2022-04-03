package com.faraz.dictionary.repository;

import com.faraz.dictionary.entity.Dictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DictionaryRepository extends MongoRepository<Dictionary, String> {

  Optional<Dictionary> findByWord(String word);

  //for aop pointcut
  @Override
  <S extends Dictionary> List<S> saveAll(Iterable<S> entities);

  //for aop pointcut
  @Override
  List<Dictionary> findAll();
}
