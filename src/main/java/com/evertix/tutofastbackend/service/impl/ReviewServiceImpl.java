package com.evertix.tutofastbackend.service.impl;

import com.evertix.tutofastbackend.exception.ResourceNotFoundException;
import com.evertix.tutofastbackend.model.Complaint;
import com.evertix.tutofastbackend.model.Review;
import com.evertix.tutofastbackend.repository.ReviewRepository;
import com.evertix.tutofastbackend.repository.UserRepository;
import com.evertix.tutofastbackend.resource.ComplaintResource;
import com.evertix.tutofastbackend.resource.ReviewResource;
import com.evertix.tutofastbackend.resource.ReviewSaveResource;
import com.evertix.tutofastbackend.service.ReviewService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public Page<ReviewResource> getAllReview(Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAll(pageable);
        List<ReviewResource> reviewResources = reviewPage.getContent().stream().map(this::convertToResource).collect(Collectors.toList());
        return new PageImpl<>(reviewResources,pageable,reviewPage.getTotalElements());

    }

    @Override
    public Page<ReviewResource> getReviewsByTeacher(Long teacherId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.getAllByTeacherId(teacherId,pageable);
        List<ReviewResource> reviewResources = reviewPage.getContent().stream().map(this::convertToResource).collect(Collectors.toList());
        return new PageImpl<>(reviewResources,pageable,reviewPage.getTotalElements());

    }

    @Override
    public ReviewResource createReview(Long studentId,Long teacherId,ReviewSaveResource newReview) {
        Review review=convertToEntity(newReview);

        return userRepository.findById(studentId).map(student ->{
            return userRepository.findById(teacherId).map(teacher ->{
                BigDecimal stars = BigDecimal.valueOf(review.getStars());
                if (teacher.getAverageStars() == null) {
                    teacher.setAverageStars(stars);
                } else {
                    Integer size=this.getTotalReviewsOfTeacher(teacherId);
                    teacher.setAverageStars(teacher.getAverageStars()
                            .multiply(BigDecimal.valueOf(size))
                            .add(stars).divide(BigDecimal.valueOf((long)size+1)));
                }
                userRepository.save(teacher);
                review.setStudent(student);
                review.setTeacher(teacher);
                return convertToResource(reviewRepository.save(review));
            }).orElseThrow(()-> new ResourceNotFoundException("Teacher with id: "+teacherId+"not found"));
        }).orElseThrow(()-> new ResourceNotFoundException("Student with id: "+studentId+"not found"));
    }

    @Override
    public ReviewResource updateReview(Long reviewId, ReviewSaveResource reviewDetails) {
        return reviewRepository.findById(reviewId).map(review ->{
            review.setDescription(reviewDetails.getDescription());
            review.setStars(reviewDetails.getStars());
            return convertToResource(reviewRepository.save(review));
        }).orElseThrow(()-> new ResourceNotFoundException("Review with id: "+reviewId+"not found"));
    }

    @Override
    public Integer getTotalReviewsOfTeacher(Long teacherId){
        return this.reviewRepository.findAllByTeacherId(teacherId).size();
    }

    @Override
    public ResponseEntity<?> deleteReview(Long reviewId) {
        return reviewRepository.findById(reviewId).map(review ->{
            reviewRepository.delete(review);
            return ResponseEntity.ok().build();
        }).orElseThrow(()-> new ResourceNotFoundException("Review with id: "+reviewId+"not found"));
    }

    private Review convertToEntity(ReviewSaveResource resource) {return mapper.map(resource,Review.class);}
    private ReviewResource convertToResource(Review entity){return mapper.map(entity, ReviewResource.class);}
}
