package com.faraz.dictionary.service;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.mailjet.client.resource.Emailv31.MESSAGES;
import static com.mailjet.client.resource.Emailv31.Message.FROM;
import static com.mailjet.client.resource.Emailv31.Message.HTMLPART;
import static com.mailjet.client.resource.Emailv31.Message.SUBJECT;
import static com.mailjet.client.resource.Emailv31.Message.TO;
import static com.mailjet.client.resource.Emailv31.resource;

@Service
public class EmailService {

  private final MailjetClient mailjetClient;
  private final String from;
  private final String to;

  public EmailService(MailjetClient mailjetClient, @Value("${mailjet.from}") String from, @Value("${mailjet.to}") String to) {
    this.mailjetClient = mailjetClient;
    this.from = from;
    this.to = to;
  }

  public int sendEmail(String subject, String body) throws MailjetSocketTimeoutException, MailjetException {
    MailjetRequest request = new MailjetRequest(resource).property(MESSAGES, new JSONArray().put(new JSONObject()
        .put(FROM, new JSONObject().put("Email", this.from).put("Name", "Personal Dictionary")).put(TO, new JSONArray()
            .put(new JSONObject().put("Email", this.to).put("Name", "Personal Dictionary"))).put(SUBJECT, subject)
        .put(HTMLPART, body)));
    MailjetResponse response = mailjetClient.post(request);
    return response.getStatus();
  }
}
