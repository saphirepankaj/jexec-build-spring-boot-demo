package com.example.demo.entity.repository;

import com.example.demo.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** @author Pankaj */
public interface UserRepository extends JpaRepository<User, Long> {

  List<User> findAllByUserName(String userName);
}
