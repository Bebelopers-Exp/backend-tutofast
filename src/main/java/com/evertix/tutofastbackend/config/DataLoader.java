package com.evertix.tutofastbackend.config;

import com.evertix.tutofastbackend.model.*;
import com.evertix.tutofastbackend.repository.*;
import com.evertix.tutofastbackend.resource.ReviewSaveResource;
import com.evertix.tutofastbackend.resource.SessionSaveResource;
import com.evertix.tutofastbackend.security.payload.request.SignUpRequest;
import com.evertix.tutofastbackend.service.AuthenticationService;
import com.evertix.tutofastbackend.service.ReviewService;
import com.evertix.tutofastbackend.service.SessionService;
import com.evertix.tutofastbackend.service.SubscriptionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.evertix.tutofastbackend.exception.ResourceNotFoundException;
import java.io.Console;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.logging.ConsoleHandler;

@Component
public class DataLoader {

    private final RoleRepository roleRepository;
    private final CourseRepository courseRepository;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionService subscriptionService;
    private final ComplaintRepository complaintRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final SessionService sessionService;
    private final WorkExperienceRepository workExperienceRepository;
    private final SessionRepository sessionRepository;
    private final SessionDetailRepository sessionDetailRepository;

    private final ModelMapper   modelMapper = new ModelMapper();

    @Autowired
    public DataLoader(RoleRepository roleRepository, CourseRepository courseRepository, AuthenticationService authenticationService,
                      UserRepository userRepository, PlanRepository planRepository, ComplaintRepository complaintRepository, SubscriptionService subscriptionService,
                      ReviewRepository reviewRepository, SessionService sessionService,WorkExperienceRepository workExperienceRepository,
                      SessionRepository sessionRepository, SessionDetailRepository sessionDetailRepository,ReviewService reviewService) {

        this.roleRepository = roleRepository;
        this.courseRepository = courseRepository;
        this.authenticationService=authenticationService;
        this.userRepository=userRepository;
        this.planRepository=planRepository;
        this.subscriptionService=subscriptionService;
        this.complaintRepository=complaintRepository;
        this.reviewRepository=reviewRepository;
        this.sessionService=sessionService;
        this.workExperienceRepository=workExperienceRepository;
        this.sessionRepository=sessionRepository;
        this.sessionDetailRepository=sessionDetailRepository;
        this.reviewService=reviewService;
        loadData();
    }

    private void loadData() {

        this.addRoles();
        this.addCourses();
        this.registerUserAdmin();
        this.registerUserStudent();
        this.registerTeacher();
        this.setTeacherCourses();
        this.addPlans();
        this.subscribeToPlan();
        this.addComplaint();
        this.addReview();
        this.createSessionRequest();
        this.createSessionHistory();


    }

    private void addPlans() {

        this.planRepository.saveAll(Arrays.asList(
                new Plan("Free","7 day of trial","You are given 4 hours of free session. You can use them within the next 5 days.",
                        (short) 4, BigDecimal.valueOf(0.00).setScale(2, RoundingMode.HALF_UP),true),
                new Plan("Basic","30 day","You are given 8 hours of sessions. You can use them in a period of 30 days",
                        (short) 8, BigDecimal.valueOf(90.50).setScale(2, RoundingMode.HALF_UP),true),
                new Plan("Platinum","30 day","You are given 12 hours of sessions. You can use them in a period of 30 days",
                        (short) 12, BigDecimal.valueOf(140.00).setScale(2, RoundingMode.HALF_UP),true),
                new Plan("Gold","30 day","You are given 20 hours of sessions. You can use them in a period of 30 days",
                        (short) 20, BigDecimal.valueOf(170.00).setScale(2, RoundingMode.HALF_UP),true),
                new Plan("Unlimited","Unlimited","You are given unlimited hours of sessions. You can use them in a period of 30 days",
                        (short) 30, BigDecimal.valueOf(250.00).setScale(2, RoundingMode.HALF_UP),true)
        ));
    }

    void addRoles(){
        //User Roles
        this.roleRepository.saveAll(Arrays.asList(
                new Role(ERole.ROLE_STUDENT),
                new Role(ERole.ROLE_TEACHER),
                new Role(ERole.ROLE_ADMIN)
        ));
    }

    void addCourses(){

        this.courseRepository.saveAll(Arrays.asList(
                new Course("Spanish", "Spanish"),
                new Course("History", "History"),
                new Course("Arithmetics", "Arithmetics"),
                new Course("Geometry", "Geometry"),
                new Course("Geography", "Geography"),
                new Course("Algebra", "Algebra"),
                new Course("Chemistry", "Chemistry"),
                new Course("Physics", "Physics"),
                new Course("Biology", "Biology")
        ));

    }

    void registerUserAdmin(){

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        SignUpRequest userAdmin = new SignUpRequest("jose.admin","password","jose@gmail.com",roles,"Jose",
                "Flores","77332217","994093798",LocalDate.now(), "Jr Monte Caoba");

        this.authenticationService.registerUser(userAdmin);

    }

    void registerUserStudent(){

        //Student Registration

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_STUDENT");
        SignUpRequest userStudent = new SignUpRequest("jesus.student","password","jesus@gmail.com",roles,"Jesus",
                "Duran","77332215","994093796", LocalDate.of(2005,8,12), "Jr Monte Algarrobo 162");

        SignUpRequest userStudent2 = new SignUpRequest("maria.student","password","maria@gmail.com",roles,"Maria",
                "Suarez","88552215","986578231", LocalDate.of(2004,4,2), "Av Caminos del Inca 205");

        this.authenticationService.registerUser(userStudent);
        this.authenticationService.registerUser(userStudent2);

    }

    void registerTeacher(){

        //Teacher Registration

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_TEACHER");
        SignUpRequest userTeacher = new SignUpRequest("albert.teacher","password","albert@gmail.com",roles,"Albert",
                "Mayta","09987745","999666555",LocalDate.of(1989,12,12), "Av Tomas Marsano 605");

        SignUpRequest userTeacher1 = new SignUpRequest("roberto.teacher","password","roberto@gmail.com",roles,"Roberto",
                "Villanueva","09822145","987456123",LocalDate.of(1991,9,18), "Av Morro Solar 125");

        this.authenticationService.registerUser(userTeacher);
        this.authenticationService.registerUser(userTeacher1);

        //Set Work Experiences for User Teacher

        WorkExperience workplace1 = new WorkExperience(LocalDate.of(2005,3,18),LocalDate.of(2010,12,18), "Colegio Alfonso Ugarte");
        workplace1.setUser(this.userRepository.findByUsername("albert.teacher").orElseThrow(()->
                new ResourceNotFoundException("User with name: albert.teacher not found")));
        WorkExperience workplace2 = new WorkExperience(LocalDate.of(2010,3,18),LocalDate.of(2018,12,18), "Universidad Tecnologica del Perú");
        workplace2.setUser(this.userRepository.findByUsername("albert.teacher").orElseThrow(()->
                new ResourceNotFoundException("User with name: albert.teacher not found")));
        WorkExperience workplace3 = new WorkExperience(LocalDate.of(2004,3,18),LocalDate.of(2009,12,18), "Colegio Trilce");
        workplace3.setUser(this.userRepository.findByUsername("roberto.teacher").orElseThrow(()->
                new ResourceNotFoundException("User with name: roberto.teacher not found")));
        WorkExperience workplace4 = new WorkExperience(LocalDate.of(2009,3,18),LocalDate.of(2019,12,18), "Universidad Mayor de San Marcos");
        workplace4.setUser(this.userRepository.findByUsername("roberto.teacher").orElseThrow(()->
                new ResourceNotFoundException("User with name: roberto.teacher not found")));

        workExperienceRepository.saveAll(Arrays.asList(workplace1,workplace2,workplace3,workplace4));


    }

    void setTeacherCourses(){



        List<Course> allCourses = courseRepository.findAll();
        User teacher1 = this.userRepository.findByUsername("albert.teacher").orElseThrow(()->
                new ResourceNotFoundException("User with name: albert.teacher not found"));
        User teacher2 = this.userRepository.findByUsername("roberto.teacher").orElseThrow(()->
                new ResourceNotFoundException("User with name: roberto.teacher not found"));

        Random rand = new Random();
        for (int i = 0; i < allCourses.size(); i++) {
            int randomIndex = rand.nextInt(allCourses.size());
            Course randomCourse = allCourses.get(randomIndex);
            if(i%2==0){
                teacher1.getCourses().add(randomCourse);
                userRepository.save(teacher1.get());
            }else {
                teacher2.getCourses().add(randomCourse);
                userRepository.save(teacher2.get());
            }

            allCourses.remove(randomIndex);
        }

    }

    void subscribeToPlan(){

        Optional<Plan> plan1 = this.planRepository.findByTitle("Free");
        Optional<Plan> plan2 = this.planRepository.findByTitle("Platinum");

        Optional<User> user1 = this.userRepository.findByUsername("jesus.student");
        Optional<User> user2 = this.userRepository.findByUsername("maria.student");

        plan1.ifPresent(planFree -> user1.ifPresent(userJesus -> this.subscriptionService.subscribeToPlan(userJesus.getId(),planFree.getId())));
        plan2.ifPresent(planPlatinium -> user2.ifPresent(userMaria -> this.subscriptionService.subscribeToPlan(userMaria.getId(),planPlatinium.getId())));


    }

    void addComplaint(){

        Optional<User> student = this.userRepository.findByUsername("jesus.student");
        Optional<User> teacher = this.userRepository.findByUsername("albert.teacher");

        Complaint complaint = new Complaint();
        complaint.setReason("Teacher didnt attend to class");
        complaint.setDescription("Teacher Albert didnt attend to class");
        student.ifPresent(student1-> teacher.ifPresent(teacher1->{
            complaint.setMadeBy(student1);
            complaint.setReported(teacher1);
        }));

        this.complaintRepository.save(complaint);

    }

    void addReview(){

        Optional<User> student = this.userRepository.findByUsername("jesus.student");
        Optional<User> teacher = this.userRepository.findByUsername("albert.teacher");

        Review review = new Review();
        review.setDescription("Really good teacher");
        review.setStars((short) 5);
        student.ifPresent(student1-> teacher.ifPresent(teacher1->{
            review.setStudent(student1);
            review.setTeacher(teacher1);
        }));


        this.reviewService.createReview(student.get().getId(), teacher.get().getId(),modelMapper.map(review, ReviewSaveResource.class));

    }

    void createSessionRequest(){

        //Create Session Request (STATUS = OPEN)
        User student1 = this.userRepository.findByUsername("jesus.student").orElseThrow(()->
                new ResourceNotFoundException("User with name: jesus.student not found"));
        Course course1 = this.courseRepository.findByName("History").orElseThrow(()->
                new ResourceNotFoundException("Course with name: History not found"));
        SessionSaveResource sessionSaveResource1=new SessionSaveResource(
                LocalDateTime.of(2020, Month.NOVEMBER, 30, 18, 30),
                LocalDateTime.of(2020, Month.NOVEMBER, 30, 20, 30),
                "World War 2",EStatus.OPEN);

        this.sessionService.createSessionRequest(course1.getId(),student1.getId(), sessionSaveResource1);


        User student2 = this.userRepository.findByUsername("maria.student").orElseThrow(()->
                new ResourceNotFoundException("User with name: maria.student not found"));
        Course course2 = this.courseRepository.findByName("Biology").orElseThrow(()->
                new ResourceNotFoundException("Course with name: Biology not found"));
        SessionSaveResource sessionSaveResource2=new SessionSaveResource(
                LocalDateTime.of(2020, Month.DECEMBER, 1, 13, 30),
                LocalDateTime.of(2020, Month.DECEMBER, 1, 17, 30),
                "Human Respiratory System",EStatus.OPEN);

        this.sessionService.createSessionRequest(course2.getId(),student2.getId(), sessionSaveResource2);

        //Make teachers apply to a random session request
        List<Session> sessionsOpen=sessionService.getAllOpenSessionRequest();

        Random rand = new Random();
        int randomIndex = rand.nextInt(sessionsOpen.size());
        Session randomSession = sessionsOpen.get(randomIndex);
        sessionService.applyToSession(randomSession.getId(),this.userRepository.findByUsername("albert.teacher").orElseThrow(()->
                new ResourceNotFoundException("User with name: albert.teacher not found")).getId());
        sessionService.applyToSession(randomSession.getId(),this.userRepository.findByUsername("roberto.teacher").orElseThrow(()->
                new ResourceNotFoundException("User with name: roberto.teacher not found")).getId());

    }

    void createSessionHistory(){

        User student2 = this.userRepository.findByUsername("maria.student").orElseThrow(()->
                new ResourceNotFoundException("User with name: maria.student not found"));
        Course course2 = this.courseRepository.findByName("Arithmetics").orElseThrow(()->
                new ResourceNotFoundException("Course with name: Biology not found"));

        Session session = new Session(LocalDateTime.of(2020, Month.NOVEMBER, 26, 13,0),
                                      LocalDateTime.of(2020, Month.NOVEMBER, 26, 15,0),
                                      EStatus.FINISHED_AND_RATED,
                                "World War 2",
                                        "www.skype.com/29",
                                        student2,
                                        course2
                                        );
        Session savedSession = this.sessionRepository.save(session);

        User teacher1 = this.userRepository.findByUsername("albert.teacher").orElseThrow(()->
                new ResourceNotFoundException("User with name: albert.teacher not found"));
        User teacher2 = this.userRepository.findByUsername("roberto.teacher").orElseThrow(()->
                new ResourceNotFoundException("User with name: roberto.teacher not found"));

        SessionDetail sessionDetail1 = new SessionDetail(false,savedSession,teacher1);
        SessionDetail sessionDetail2 = new SessionDetail(true,savedSession,teacher2);

        this.sessionDetailRepository.saveAll(Arrays.asList(sessionDetail1,sessionDetail2));





    }


}


