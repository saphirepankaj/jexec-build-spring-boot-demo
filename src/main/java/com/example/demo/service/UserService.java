package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** @author Pankaj */
@Service
public class UserService {

  @Autowired private UserRepository userRepository;

  public List<User> users() {
    return userRepository.findAll();
  }

  public List<User> users(String userName) {
    return userRepository.findAllByUserName(userName);
  }

  public User user(Long id) {
    return userRepository.findById(id).orElse(null);
  }

  public User save(User user) {
    return userRepository.save(user);
  }
}
