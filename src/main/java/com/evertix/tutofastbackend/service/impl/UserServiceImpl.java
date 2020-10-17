package com.evertix.tutofastbackend.service.impl;

import com.evertix.tutofastbackend.exception.ResourceNotFoundException;
import com.evertix.tutofastbackend.model.Course;
import com.evertix.tutofastbackend.model.ERole;
import com.evertix.tutofastbackend.model.Role;
import com.evertix.tutofastbackend.model.User;
import com.evertix.tutofastbackend.repository.CourseRepository;
import com.evertix.tutofastbackend.repository.RoleRepository;
import com.evertix.tutofastbackend.repository.UserRepository;
import com.evertix.tutofastbackend.security.payload.response.MessageResponse;
import com.evertix.tutofastbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public boolean userExistsByUsername(String username) {
        return this.userRepository.existsByUsername(username);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    @Override
    public User createUser(User user) {
        return this.userRepository.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(()->
                new ResourceNotFoundException("User with Id: "+userId+" not found"));
    }

    @Override
    public User updateUser(Long userId, User userDetails) {
        return userRepository.findById(userId).map(user -> {
            user.setUsername(userDetails.getUsername());
            user.setPassword(userDetails.getPassword());
            user.setName(userDetails.getName());
            user.setLastName(userDetails.getLastName());
            user.setBirthday(userDetails.getBirthday());
            user.setPhone(userDetails.getPhone());
            user.setAddress(userDetails.getAddress());
            user.setAverageStars(userDetails.getAverageStars());
            user.setActive(userDetails.isActive());
            user.setLinkedin(userDetails.getLinkedin());
            return userRepository.save(user);
        }).orElseThrow(()-> new ResourceNotFoundException("User whit Id: "+userId+" not found"));
    }

    @Override
    public ResponseEntity<?> deleteUser(Long userId) {
        return userRepository.findById(userId).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok().build();
        }).orElseThrow(()-> new ResourceNotFoundException("User with Id: "+userId+" not found"));
    }

    @Override
    public User setLinkedinProfile(Long userId,String linkedin) {
        User user = this.userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User with Id: "+userId+" not found"));
        user.setLinkedin(linkedin);
        return this.userRepository.save(user);

    }

    @Override
    public ResponseEntity<?> addCourses(Long userId, List<Long> coursesId) {
        User user = this.userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User with Id: "+userId+" not found"));
        List<Course> coursesList = new ArrayList<>();
        Optional<Role> role = this.roleRepository.findByName(ERole.ROLE_TEACHER);
        if (user.getRoles().contains(role.orElse(null))){
            for(Long courseId: coursesId){
                Course course = this.courseRepository.findById(courseId).orElseThrow(()-> new ResourceNotFoundException("Course with Id: "+courseId+" not found"));
                if(!user.getCourses().contains(course)){
                    coursesList.add(course);
                }
            }
            user.setCourses(coursesList);
            return ResponseEntity.ok(userRepository.save(user));
        }else {
            return ResponseEntity.badRequest().body(new MessageResponse("You can set course only for teachers"));
        }
    }

    @Override
    public User removeCourses(Long userId, List<Long> coursesId) {
        User user = this.userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User with Id: "+userId+" not found"));
        for(Long courseId: coursesId){
            Course course = this.courseRepository.findById(courseId).orElseThrow(()-> new ResourceNotFoundException("Course with Id: "+courseId+" not found"));
            user.getCourses().remove(course);
        }
        return userRepository.save(user);
    }

    @Override
    public ResponseEntity<?> banUser(Long userId) {
        return ResponseEntity.ok(
                this.userRepository.findById(userId).map(user -> {
                    user.setActive(false);
                    return userRepository.save(user);
                }).orElseThrow(()-> new ResourceNotFoundException("User with Id: "+userId+" not found"))
        );
    }

}
