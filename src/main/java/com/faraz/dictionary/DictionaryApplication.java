package com.faraz.dictionary;

import com.faraz.dictionary.service.EmailService;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.CronTask;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
public class DictionaryApplication implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(DictionaryApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(DictionaryApplication.class, args);
  }

  @Autowired
  ScheduledAnnotationBeanPostProcessor scheduledAnnotationBeanPostProcessor;

  @Override
  public void run(String... args) throws Exception {
    scheduledAnnotationBeanPostProcessor.getScheduledTasks().stream().map(task -> task.getTask()).filter(
            CronTask.class::isInstance).map(CronTask.class::cast)
        .forEach(task -> logger.info("Task Name: {}, Task Time: {}", task, task.getExpression()));
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory mongoDbFactory,
                                                     MongoMappingContext mongoMappingContext) {
    DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
    MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
    converter.setTypeMapper(new DefaultMongoTypeMapper(null));
    return converter;
  }

  @Bean
  public TaskSchedulerCustomizer taskSchedular(EmailService emailService) {
    return customizer -> {
      customizer.setErrorHandler(new ScheduledErrorHandler(emailService));
    };
  }

  @Bean
  public MailjetClient mailjetClient(@Value("${mailjet.apiKey}") String mailjetApiKey,
                                     @Value("${mailjet.apiSecret}") String mailjetApiSecret) {
    return new MailjetClient(mailjetApiKey, mailjetApiSecret, new ClientOptions("v3.1"));
  }

}
