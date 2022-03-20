package com.faraz.dictionary.service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private final String mailjetApiKey;
  private final String mailjetApiSecret;
  private final String from;
  private final String to;

  public EmailService(@Value("${mailjet.apiKey}") String mailjetApiKey,
                      @Value("${mailjet.apiSecret}") String mailjetApiSecret,
                      @Value("${mailjet.from}") String from, @Value("${mailjet.to}") String to) {
    this.mailjetApiKey = mailjetApiKey;
    this.mailjetApiSecret = mailjetApiSecret;
    this.from = from;
    this.to = to;
  }

  public void sendEmail(String subject, String body) throws MailjetSocketTimeoutException, MailjetException {
    MailjetClient client = new MailjetClient(mailjetApiKey, mailjetApiSecret, new ClientOptions("v3.1"));
    MailjetRequest request = new MailjetRequest(Emailv31.resource).property(Emailv31.MESSAGES,
        new JSONArray().put(new JSONObject().put(Emailv31.Message.FROM,
            new JSONObject().put("Email", "faraz.uic2@gmail.com").put("Name", "Personal Dictionary")).put(
            Emailv31.Message.TO, new JSONArray().put(
                new JSONObject().put("Email", "faraz.uic2@gmail.com").put("Name",
                    "Personal Dictionary"))).put(Emailv31.Message.SUBJECT, subject).put(
            Emailv31.Message.HTMLPART, body)));
    MailjetResponse response = client.post(request);
    System.out.println(response.getStatus());
    System.out.println(response.getData());
  }
}
