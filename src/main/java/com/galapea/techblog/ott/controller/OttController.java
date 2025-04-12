package com.galapea.techblog.ott.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/ott")
@Slf4j
public class OttController {
  @GetMapping("/sent")
  public String sent(Model model) {
    return "ott/sent";
  }

  @GetMapping("/submit")
  public String submit(Model model, @RequestParam("token") String token) {
    System.out.println("=========Token: " + token);
    model.addAttribute("token", token);
    return "ott/submit";
  }
}
