package com.faraz.dictionary.controller;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

@RestController
public class HealthController {

  private final RestTemplate restTemplate;
  private final MongoTemplate mongoTemplate;
  private final String merriamWebsterKey;
  private final String merriamWebsterUrl;
  private final String freeDictionaryEndpoint;
  private final String mailjetApiKey;
  private final String mailjetApiSecret;

  public HealthController(RestTemplate restTemplate, MongoTemplate mongoTemplate,
                          @Value("${dictionary.merriamWebster.key}") String merriamWebsterKey,
                          @Value("${dictionary.merriamWebster.url}") String merriamWebsterUrl,
                          @Value("${dictionary.freeDictionary.url}") String freeDictionaryEndpoint,
                          @Value("${mailjet.apiKey}") String mailjetApiKey,
                          @Value("${mailjet.apiSecret}") String mailjetApiSecret) {
    this.restTemplate = restTemplate;
    this.mongoTemplate = mongoTemplate;
    this.merriamWebsterKey = merriamWebsterKey;
    this.merriamWebsterUrl = merriamWebsterUrl;
    this.freeDictionaryEndpoint = freeDictionaryEndpoint;
    this.mailjetApiKey = mailjetApiKey;
    this.mailjetApiSecret = mailjetApiSecret;
  }

  //todo work on health
  //check status after making calls to dictionaries and thats enough
  //append thefreedictionary check and result
  @GetMapping({"/", "/health"})
  public String health() throws MailjetSocketTimeoutException, MailjetException {
    if (false) {
      sendEmail();
      return "sending email";
    }
    ResponseEntity<String> response = restTemplate.getForEntity(
        String.format(merriamWebsterUrl, "word", merriamWebsterKey), String.class);
    String json = response.getBody();
    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
    StringBuilder health = new StringBuilder();
    if (!flattenJson.isEmpty()) {
      health.append("Merriam Webster Dictionary is up. ").append(System.lineSeparator());
    }
    mongoTemplate.getCollection("dictionary").countDocuments();
    //above line didn't throw error so mongo is up.
    health.append("Mongo is up.");


    return health.toString();
  }

  private void sendEmail() throws MailjetSocketTimeoutException, MailjetException {
    MailjetClient client = new MailjetClient(mailjetApiKey, mailjetApiSecret, new ClientOptions("v3.1"));
    MailjetRequest request = new MailjetRequest(Emailv31.resource).property(Emailv31.MESSAGES,
        new JSONArray().put(new JSONObject().put(Emailv31.Message.FROM,
            new JSONObject().put("Email", "faraz.uic2@gmail.com").put("Name", "Personal Dictionary")).put(
            Emailv31.Message.TO, new JSONArray().put(
                new JSONObject().put("Email", "faraz.uic2@gmail.com").put("Name",
                    "Personal Dictionary"))).put(Emailv31.Message.SUBJECT, "Testing Sending Email").put(
            Emailv31.Message.TEXTPART, "My first Mailjet email").put(Emailv31.Message.HTMLPART,
            "Bismillah").put(Emailv31.Message.CUSTOMID, "AppGettingStartedTest")));
    MailjetResponse response = client.post(request);
    System.out.println(response.getStatus());
    System.out.println(response.getData());
  }

  @GetMapping("favicon.ico")
  void returnNoFavicon() {
  }

}
