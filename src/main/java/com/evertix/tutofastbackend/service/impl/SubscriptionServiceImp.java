package com.evertix.tutofastbackend.service.impl;

import com.evertix.tutofastbackend.exception.ResourceNotFoundException;
import com.evertix.tutofastbackend.model.*;
import com.evertix.tutofastbackend.repository.PlanRepository;
import com.evertix.tutofastbackend.repository.RoleRepository;
import com.evertix.tutofastbackend.repository.SubscriptionRepository;
import com.evertix.tutofastbackend.repository.UserRepository;
import com.evertix.tutofastbackend.security.payload.response.MessageResponse;
import com.evertix.tutofastbackend.service.SubscriptionService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionServiceImp implements SubscriptionService {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PlanRepository planRepository;

    @Autowired
    RoleRepository roleRepository;

    @Override
    public Page<Subscription> getAllSubscriptions(Pageable pageable) {
        return subscriptionRepository.findAll(pageable);
    }

    @Override
    public Page<Subscription> getUsersSubscriptions(Long userId, Pageable pageable) {
        return subscriptionRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public ResponseEntity<?> subscribeToPlan(Long userId, Long planId) {
        User user = this.userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User with id: "+userId+" not found"));

        Plan plan = this.planRepository.findById(planId).orElseThrow(()-> new ResourceNotFoundException("Plan with id: "+planId+" not found"));

        Optional<Role> role = this.roleRepository.findByName(ERole.ROLE_STUDENT);


        boolean var=false;
        for(Role roli: user.getRoles()){
            if (roli.getName().equals(ERole.ROLE_STUDENT)){
                var=true;
            };
        }

        //Check if user is student
        if (var) {

            //Check user has subscribe to any plan
            List<Subscription> userSubsHistory = this.subscriptionRepository.findAllByUserId(userId);
            if (userSubsHistory.size() > 0) {
                for (Subscription subscription : userSubsHistory) {
                    if (subscription.getActive()) {
                        this.unsubscribe(subscription.getId());
                    }
                }
            }
            Subscription savedSubscription= this.subscribeUserToPlan(user.get(),plan);
            this.reloadHoursCredit(user,plan);
            return ResponseEntity.ok(savedSubscription);
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Only student can subscribe to a plan"));
        }


    }

    @Override
    public ResponseEntity<?> unsubscribeToPlan(Long userId, Long planId) {

        User user = this.userRepository.findById(userId).
                orElseThrow(() -> new ResourceNotFoundException("User with Id: " + userId + " not found"));

        Plan plan = this.planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan with id: " + planId + " not found"));

        Optional<Role> role = this.roleRepository.findByName(ERole.ROLE_STUDENT);
        //Check if user is student
        if (user.getRoles().contains(role.orElse(null))) {
            //Check user has subscribe to any plan
            List<Subscription> userSubsHistory = this.subscriptionRepository.findAllByUserId(userId);
            if (userSubsHistory.size() == 0) {
                return ResponseEntity.badRequest().body(new MessageResponse("User is not subscribe to any plan"));
            } else {
                //Verify is there is an active subscription and unsubscribe to it (set active to false)
                ResponseEntity<?> response= ResponseEntity.badRequest().body(new MessageResponse("User subscription to this plan is not active"));
                for (Subscription subscription : userSubsHistory) {
                    if(subscription.getPlan().getId().equals(planId) & subscription.getActive()) {
                        response=null;
                        response=ResponseEntity.ok(this.unsubscribe(subscription.getId()));
                    }
                }
                return ResponseEntity.ok(response);
            }

        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Only student can unsubscribe to a plan"));
        }

    }

    @Override
    public ResponseEntity<?> deleteSubscription(Long userId, Long planId, Long subscriptionId) {
        return userRepository.findById(userId).map(user -> {
            return planRepository.findById(planId).map(plan -> {
                return subscriptionRepository.findById(subscriptionId).map(subscription -> {
                    subscriptionRepository.delete(subscription);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Subscription with id: " + subscriptionId + " not found"));
            }).orElseThrow(() -> new ResourceNotFoundException("Plan with id: " + planId + " not found"));
        }).orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found"));
    }

    public Subscription subscribeUserToPlan(User user,Plan plan){
        Subscription subscription = new Subscription();
        subscription.setActive(true);
        subscription.setPlan(plan);
        subscription.setUser(user);
        return subscriptionRepository.save(subscription);
    }

    public void reloadHoursCredit(User user, Plan plan){
        user.setCreditHours(plan.getHours());
        this.userRepository.save(user);
    }

    public Subscription unsubscribe(Long subscriptionId){
        Subscription subscription=this.subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription with id: " + subscriptionId + " not found"));
        subscription.setActive(false);
        return this.subscriptionRepository.save(subscription);

    }
}
