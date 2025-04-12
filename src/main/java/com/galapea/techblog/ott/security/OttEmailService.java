package com.galapea.techblog.ott.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OttEmailService {

  public void sendEmail(String to, String ottLink) {
    log.info("<<<<<<<< Sending OTT Email to {} >>>>>", to);
    log.info("<<<<<<<< Link to sign in: {} >>>>>", ottLink);
  }
}
