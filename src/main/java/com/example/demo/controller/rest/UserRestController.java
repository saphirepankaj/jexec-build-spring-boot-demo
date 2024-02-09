package com.example.demo.controller.rest;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** @author Pankaj */
@RestController
@RequestMapping(value = "/api/v1/users")
public class UserRestController {

  @Autowired private UserService userService;

  @GetMapping
  public List<User> get() {
    return userService.users();
  }

  @GetMapping(value = "/by/userName/{userName}")
  public List<User> get(@PathVariable String userName) {
    return userService.users(userName);
  }

  @GetMapping(value = "/{id}")
  public User get(@PathVariable Long id) {
    return userService.user(id);
  }

  @PostMapping
  public User post(@RequestBody User user) {
    return userService.save(user);
  }
}
