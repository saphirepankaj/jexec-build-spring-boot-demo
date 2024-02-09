package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/")
public class HomePageController {

  @GetMapping
  public String index(Model model) {
    model.addAttribute("msg", "Hello World");
    return "home";
  }
}
