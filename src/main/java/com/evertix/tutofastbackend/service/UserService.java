package com.evertix.tutofastbackend.service;

import com.evertix.tutofastbackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    boolean userExistsByUsername(String username);
    boolean userExistsByEmail(String email);
    User createUser(User user);
    User getUserById(Long userId);
    User updateUser(Long userId, User userDetails);
    ResponseEntity<?> deleteUser(Long userId);
    User setLinkedinProfile(Long userId,String linkedin);

    ResponseEntity<?> addCourses(Long userId, List<Long> coursesId);
    User removeCourses(Long userId, List<Long> coursesId);

    ResponseEntity<?> banUser(Long userId);
}
