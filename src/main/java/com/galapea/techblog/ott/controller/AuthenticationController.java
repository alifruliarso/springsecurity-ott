package com.galapea.techblog.ott.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/authentication")
public class AuthenticationController {

  @GetMapping("/login")
  public String login() {
    return "authentication/login";
  }
}
