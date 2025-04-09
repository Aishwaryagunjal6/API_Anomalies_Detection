package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "alert.email")
public class EmailConfig {

  private List<String> recipients;

  public List<String> getRecipients() {
    return recipients;
  }

  public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }
}
