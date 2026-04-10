package com.classgo.backend.application.learning;

import com.classgo.backend.application.notifications.NotificationService;
import com.classgo.backend.api.learning.dto.LearningDtos.ActivateWeekRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.ActivateWeekResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.AddPlanTopicRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.AssignPlanRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.AvatarResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.BasicUserResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.ClassroomResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.ClassroomWithDetailsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.ClassroomPresenceEventResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.CreateClassroomRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.CreatePlanRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.CreateTopicRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.GameplayContextResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.HistoryEntryResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.InAppNotificationResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.JoinClassroomRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.LeaderboardEntryResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.PlanResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.ReorderPlanTopicsRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.StudentResultResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.StudentResultWithDetailsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.SubmitResultRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.TeacherClassroomDetailResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.TopicResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdateClassroomRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdatePlanRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdateTopicRequest;
import com.classgo.backend.domain.enums.ActivationMode;
import com.classgo.backend.domain.enums.NotificationType;
import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.domain.model.AppClassroom;
import com.classgo.backend.domain.model.Enrollment;
import com.classgo.backend.domain.model.LearningTopic;
import com.classgo.backend.domain.model.PlanTopic;
import com.classgo.backend.domain.model.StudentAttempt;
import com.classgo.backend.domain.model.StudyPlan;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.repository.AppClassroomRepository;
import com.classgo.backend.domain.repository.AvatarCatalogRepository;
import com.classgo.backend.domain.repository.EnrollmentRepository;
import com.classgo.backend.domain.repository.LearningTopicRepository;
import com.classgo.backend.domain.repository.PlanTopicRepository;
import com.classgo.backend.domain.repository.StudentAttemptRepository;
import com.classgo.backend.domain.repository.StudyPlanRepository;
import com.classgo.backend.domain.repository.UserRepository;
import com.classgo.backend.infrastructure.security.SecurityUtils;
import com.classgo.backend.shared.exception.BusinessRuleViolationException;
import com.classgo.backend.shared.exception.DuplicateResourceException;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import com.classgo.backend.shared.exception.UnauthorizedOperationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class LearningPlatformService {

    private final UserRepository userRepository;
    private final AvatarCatalogRepository avatarCatalogRepository;
    private final AppClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudyPlanRepository planRepository;
    private final LearningTopicRepository topicRepository;
    private final PlanTopicRepository planTopicRepository;
    private final StudentAttemptRepository studentAttemptRepository;
    private final LearningSupport support;
    private final NotificationService notificationService;
    private final ClassroomPresenceStreamService classroomPresenceStreamService;

    public LearningPlatformService(
        UserRepository userRepository,
        AvatarCatalogRepository avatarCatalogRepository,
        AppClassroomRepository classroomRepository,
        EnrollmentRepository enrollmentRepository,
        StudyPlanRepository planRepository,
        LearningTopicRepository topicRepository,
        PlanTopicRepository planTopicRepository,
        StudentAttemptRepository studentAttemptRepository,
        LearningSupport support,
        NotificationService notificationService,
        ClassroomPresenceStreamService classroomPresenceStreamService
    ) {
        this.userRepository = userRepository;
        this.avatarCatalogRepository = avatarCatalogRepository;
        this.classroomRepository = classroomRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.planRepository = planRepository;
        this.topicRepository = topicRepository;
        this.planTopicRepository = planTopicRepository;
        this.studentAttemptRepository = studentAttemptRepository;
        this.support = support;
        this.notificationService = notificationService;
        this.classroomPresenceStreamService = classroomPresenceStreamService;
    }

    @Transactional(readOnly = true)
    public List<AvatarResponse> avatars() {
        return avatarCatalogRepository.findAllByOrderBySortOrderAsc().stream().map(support::avatarResponse).toList();
    }

    @Transactional
    public ClassroomResponse createClassroom(CreateClassroomRequest request) {
        support.requireTeacher();
        String normalizedCode = normalizeCode(request.code());
        if (classroomRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new DuplicateResourceException("CLASSROOM_CODE_ALREADY_EXISTS", "The classroom code already exists");
        }
        User teacher = currentUser();
        AppClassroom classroom = new AppClassroom();
        classroom.setTeacher(teacher);
        classroom.setName(request.name().trim());
        classroom.setCode(normalizedCode);
        classroom.setDescription(request.description());
        classroom.setCurrentWeek(0);
        return support.classroomResponse(classroomRepository.save(classroom));
    }

    @Transactional
    public ClassroomResponse updateClassroom(UUID classroomId, UpdateClassroomRequest request) {
        support.requireTeacher();
        AppClassroom classroom = ownedClassroom(classroomId);
        if (request.name() != null) {
            classroom.setName(request.name().trim());
        }
        if (request.description() != null) {
            classroom.setDescription(request.description());
        }
        if (request.code() != null) {
            String normalizedCode = normalizeCode(request.code());
            if (!normalizedCode.equalsIgnoreCase(classroom.getCode()) && classroomRepository.existsByCodeIgnoreCase(normalizedCode)) {
                throw new DuplicateResourceException("CLASSROOM_CODE_ALREADY_EXISTS", "The classroom code already exists");
            }
            classroom.setCode(normalizedCode);
        }
        if (request.activePlanId() != null) {
            classroom.setActivePlan(ownedPlan(request.activePlanId()));
        }
        if (request.currentWeek() != null) {
            classroom.setCurrentWeek(request.currentWeek());
        }
        return support.classroomResponse(classroomRepository.save(classroom));
    }

    @Transactional
    public void deleteClassroom(UUID classroomId) {
        support.requireTeacher();
        classroomRepository.delete(ownedClassroom(classroomId));
    }

    @Transactional(readOnly = true)
    public List<ClassroomWithDetailsResponse> teacherClassrooms() {
        support.requireTeacher();
        return classroomRepository.findAllByTeacherOrderByCreatedAtDesc(currentUser()).stream().map(this::classroomDetails).toList();
    }

    @Transactional(readOnly = true)
    public ClassroomWithDetailsResponse classroom(UUID classroomId) {
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        authorizeClassroomView(classroom);
        return classroomDetails(classroom);
    }

    @Transactional
    public ClassroomWithDetailsResponse assignPlan(UUID classroomId, AssignPlanRequest request) {
        support.requireTeacher();
        AppClassroom classroom = ownedClassroom(classroomId);
        if (request.planId() == null) {
            classroom.setActivePlan(null);
            classroom.setCurrentWeek(0);
            return classroomDetails(classroomRepository.save(classroom));
        }
        StudyPlan plan = ownedPlan(request.planId());
        classroom.setActivePlan(plan);
        if (classroom.getCurrentWeek() == 0) {
            classroom.setCurrentWeek(1);
        }
        activateWeekInternal(plan, 1, false);
        return classroomDetails(classroomRepository.save(classroom));
    }

    @Transactional
    public ClassroomWithDetailsResponse joinClassroom(JoinClassroomRequest request) {
        support.requireStudent();
        AppClassroom classroom = classroomRepository.findByCodeIgnoreCase(normalizeCode(request.code()))
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        if (!enrollmentRepository.existsByClassroomIdAndStudentId(classroom.getId(), SecurityUtils.currentUserId())) {
            Enrollment enrollment = new Enrollment();
            enrollment.setClassroom(classroom);
            enrollment.setStudent(currentUser());
            enrollment.setJoinedAt(Instant.now());
            enrollmentRepository.save(enrollment);
        }
        return classroomDetails(classroom);
    }

    @Transactional(readOnly = true)
    public List<ClassroomWithDetailsResponse> studentClassrooms() {
        support.requireStudent();
        return enrollmentRepository.findAllByStudentIdOrderByJoinedAtDesc(SecurityUtils.currentUserId()).stream()
            .map(Enrollment::getClassroom)
            .distinct()
            .map(this::classroomDetails)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<BasicUserResponse> classroomStudents(UUID classroomId) {
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        authorizeClassroomView(classroom);
        return enrolledUsers(classroom).stream().map(support::basicUserResponse).toList();
    }

    @Transactional
    public void enrollStudent(UUID classroomId, UUID studentId) {
        support.requireTeacher();
        AppClassroom classroom = ownedClassroom(classroomId);
        User student = support.requireUser(studentId, userRepository);
        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessRuleViolationException("STUDENT_ROLE_REQUIRED", "The user must be a student");
        }
        if (enrollmentRepository.existsByClassroomIdAndStudentId(classroomId, studentId)) {
            return;
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setClassroom(classroom);
        enrollment.setStudent(student);
        enrollment.setJoinedAt(Instant.now());
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void removeEnrollment(UUID classroomId, UUID studentId) {
        support.requireTeacher();
        ownedClassroom(classroomId);
        enrollmentRepository.deleteByClassroomIdAndStudentId(classroomId, studentId);
    }

    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        support.requireTeacher();
        validatePlanRequest(request.activationMode(), request.startDate());
        StudyPlan plan = new StudyPlan();
        plan.setTeacher(currentUser());
        plan.setName(request.name().trim());
        plan.setDescription(request.description());
        plan.setActivationMode(request.activationMode());
        plan.setStartDate(request.startDate());
        plan = planRepository.save(plan);
        return support.planResponse(plan, List.of());
    }

    @Transactional
    public PlanResponse updatePlan(UUID planId, UpdatePlanRequest request) {
        support.requireTeacher();
        StudyPlan plan = ownedPlan(planId);
        ActivationMode activationMode = request.activationMode() != null ? request.activationMode() : plan.getActivationMode();
        LocalDate startDate = request.startDate() != null ? request.startDate() : plan.getStartDate();
        validatePlanRequest(activationMode, startDate);
        if (request.name() != null) {
            plan.setName(request.name().trim());
        }
        if (request.description() != null) {
            plan.setDescription(request.description());
        }
        if (request.activationMode() != null) {
            plan.setActivationMode(request.activationMode());
        }
        if (request.activationMode() == ActivationMode.MANUAL) {
            plan.setStartDate(null);
        } else if (request.startDate() != null) {
            plan.setStartDate(request.startDate());
        }
        return support.planResponse(planRepository.save(plan), planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(planId));
    }

    @Transactional
    public void deletePlan(UUID planId) {
        support.requireTeacher();
        StudyPlan plan = ownedPlan(planId);
        if (classroomRepository.countByActivePlanId(planId) > 0) {
            throw new BusinessRuleViolationException("PLAN_IN_USE", "The plan cannot be deleted because it is assigned to a classroom");
        }
        planRepository.delete(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> teacherPlans() {
        support.requireTeacher();
        return planRepository.findAllByTeacherIdOrderByCreatedAtDesc(SecurityUtils.currentUserId()).stream()
            .map(plan -> support.planResponse(plan, planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(plan.getId())))
            .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse plan(UUID planId) {
        StudyPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("PLAN_NOT_FOUND", "Plan not found"));
        if (SecurityUtils.currentUser().role() == UserRole.TEACHER && !plan.getTeacher().getId().equals(SecurityUtils.currentUserId())) {
            throw new UnauthorizedOperationException("FORBIDDEN", "You cannot view another teacher's plans");
        }
        return support.planResponse(plan, planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(planId));
    }

    @Transactional
    public TopicResponse createTopic(CreateTopicRequest request) {
        support.requireTeacher();
        var normalizedQuestions = support.normalizeAndValidateQuestions(request.questions());
        LearningTopic topic = new LearningTopic();
        topic.setTeacher(currentUser());
        topic.setName(request.name().trim());
        topic.setDescription(request.description());
        topic.setIcon(request.icon());
        topic.setColor(request.color());
        topic.setDifficulty(request.difficulty());
        topic.setQuestionsJson(support.writeJson(normalizedQuestions));
        return support.topicResponse(topicRepository.save(topic));
    }

    @Transactional
    public TopicResponse updateTopic(UUID topicId, UpdateTopicRequest request) {
        support.requireTeacher();
        LearningTopic topic = ownedTopic(topicId);
        if (request.questions() != null) {
            var normalizedQuestions = support.normalizeAndValidateQuestions(request.questions());
            topic.setQuestionsJson(support.writeJson(normalizedQuestions));
        }
        if (request.name() != null) {
            topic.setName(request.name().trim());
        }
        if (request.description() != null) {
            topic.setDescription(request.description());
        }
        if (request.icon() != null) {
            topic.setIcon(request.icon());
        }
        if (request.color() != null) {
            topic.setColor(request.color());
        }
        if (request.difficulty() != null) {
            topic.setDifficulty(request.difficulty());
        }
        return support.topicResponse(topicRepository.save(topic));
    }

    @Transactional
    public void deleteTopic(UUID topicId) {
        support.requireTeacher();
        LearningTopic topic = ownedTopic(topicId);
        if (planTopicRepository.countByTopicId(topicId) > 0) {
            throw new BusinessRuleViolationException("TOPIC_IN_USE", "The topic cannot be deleted because it is assigned to a plan");
        }
        topicRepository.delete(topic);
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> teacherTopics() {
        support.requireTeacher();
        return topicRepository.findAllByTeacherIdOrderByCreatedAtDesc(SecurityUtils.currentUserId()).stream().map(support::topicResponse).toList();
    }

    @Transactional(readOnly = true)
    public TopicResponse topic(UUID topicId) {
        LearningTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new ResourceNotFoundException("TOPIC_NOT_FOUND", "Topic not found"));
        if (SecurityUtils.currentUser().role() == UserRole.TEACHER && !topic.getTeacher().getId().equals(SecurityUtils.currentUserId())) {
            throw new UnauthorizedOperationException("FORBIDDEN", "You cannot view another teacher's topics");
        }
        return support.topicResponse(topic);
    }

    @Transactional
    public PlanResponse addTopicToPlan(UUID planId, AddPlanTopicRequest request) {
        support.requireTeacher();
        StudyPlan plan = ownedPlan(planId);
        LearningTopic topic = ownedTopic(request.topicId());
        if (planTopicRepository.existsByPlanIdAndTopicId(planId, topic.getId())) {
            throw new BusinessRuleViolationException("PLAN_TOPIC_ALREADY_EXISTS", "The topic already exists in the plan");
        }
        int weekNumber = request.weekNumber() != null ? request.weekNumber() : planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(planId).size() + 1;
        if (planTopicRepository.existsByPlanIdAndWeekNumber(planId, weekNumber)) {
            throw new BusinessRuleViolationException("PLAN_WEEK_ALREADY_EXISTS", "The week already exists in the plan");
        }
        PlanTopic planTopic = new PlanTopic();
        planTopic.setPlan(plan);
        planTopic.setTopic(topic);
        planTopic.setWeekNumber(weekNumber);
        planTopic.setActive(false);
        planTopicRepository.save(planTopic);
        return support.planResponse(plan, planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(planId));
    }

    @Transactional
    public PlanResponse removeTopicFromPlan(UUID planId, UUID topicId) {
        support.requireTeacher();
        ownedPlan(planId);
        planTopicRepository.deleteByPlanIdAndTopicId(planId, topicId);
        normalizePlanWeeks(planId);
        StudyPlan plan = ownedPlan(planId);
        return support.planResponse(plan, planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(planId));
    }

    @Transactional
    public PlanResponse reorderPlanTopics(UUID planId, ReorderPlanTopicsRequest request) {
        support.requireTeacher();
        ownedPlan(planId);
        List<PlanTopic> items = planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(planId);
        if (items.size() != request.orderedTopicIds().size()) {
            throw new BusinessRuleViolationException("PLAN_REORDER_INVALID", "orderedTopicIds does not match the plan topics");
        }
        Map<UUID, PlanTopic> byTopic = new HashMap<>();
        items.forEach(item -> byTopic.put(item.getTopic().getId(), item));
        for (int i = 0; i < request.orderedTopicIds().size(); i++) {
            UUID topicId = request.orderedTopicIds().get(i);
            PlanTopic item = byTopic.get(topicId);
            if (item == null) {
                throw new BusinessRuleViolationException("PLAN_REORDER_INVALID", "orderedTopicIds contains invalid topics");
            }
            item.setWeekNumber(i + 1);
        }
        planTopicRepository.saveAll(items);
        StudyPlan plan = ownedPlan(planId);
        return support.planResponse(plan, planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(planId));
    }

    @Transactional
    public ActivateWeekResponse activateWeek(UUID planId, ActivateWeekRequest request) {
        support.requireTeacher();
        StudyPlan plan = ownedPlan(planId);
        activateWeekInternal(plan, request.weekNumber(), false);
        return new ActivateWeekResponse(planId, request.weekNumber());
    }

    @Transactional
    public GameplayContextResponse gameplayContext(UUID classroomId) {
        support.requireStudent();
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        ensureStudentEnrolled(classroom);
        if (classroom.getActivePlan() == null) {
            throw new ResourceNotFoundException("ACTIVE_PLAN_NOT_FOUND", "The classroom has no active plan");
        }
        PlanTopic activeWeek = planTopicRepository.findByPlanIdAndActiveTrue(classroom.getActivePlan().getId())
            .orElseThrow(() -> new ResourceNotFoundException("ACTIVE_TOPIC_NOT_FOUND", "There is no active topic"));
        StudentAttempt bestAttempt = bestAttempt(studentAttemptRepository.findAllByStudentIdAndClassroomIdAndTopicIdAndWeekNumberOrderByCompletedAtDesc(
            SecurityUtils.currentUserId(),
            classroomId,
            activeWeek.getTopic().getId(),
            activeWeek.getWeekNumber()
        ));
        return new GameplayContextResponse(
            support.gameplayClassroomResponse(classroom),
            support.gameplayTopicResponse(activeWeek.getTopic()),
            true,
            bestAttempt != null ? support.resultResponse(bestAttempt) : null,
            classroomNotifications(classroom.getId())
        );
    }

    @Transactional(readOnly = true)
    public SseEmitter subscribeToClassroomPresence(UUID classroomId) {
        support.requireStudent();
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        ensureStudentEnrolled(classroom);
        User student = currentUser();
        return classroomPresenceStreamService.subscribe(classroomId, student.getId(), student.getName(), student.getAvatarId());
    }

    @Transactional
    public void connectClassroomPresence(UUID classroomId) {
        support.requireStudent();
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        ensureStudentEnrolled(classroom);
        User student = currentUser();
        boolean becameOnline = classroomPresenceStreamService.connect(classroomId, student.getId(), student.getName(), student.getAvatarId());
        classroomPresenceStreamService.heartbeat(classroomId, student.getId());
        if (becameOnline) {
            notifyClassroomPeersStudentConnected(classroom, student);
            classroomPresenceStreamService.publishStudentConnected(classroomId, student.getId());
        }
    }

    @Transactional
    public void heartbeatClassroomPresence(UUID classroomId) {
        support.requireStudent();
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        ensureStudentEnrolled(classroom);
        classroomPresenceStreamService.heartbeat(classroomId, SecurityUtils.currentUserId());
    }

    @Transactional
    public void disconnectClassroomPresence(UUID classroomId) {
        support.requireStudent();
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        ensureStudentEnrolled(classroom);
        classroomPresenceStreamService.disconnect(classroomId, SecurityUtils.currentUserId());
    }

    @Transactional(readOnly = true)
    public List<InAppNotificationResponse> classroomNotifications(UUID classroomId) {
        support.requireStudent();
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        ensureStudentEnrolled(classroom);
        return notificationService.inAppNotifications(SecurityUtils.currentUserId()).stream()
            .filter(notification -> notification.getReadAt() == null)
            .filter(notification -> notification.getType() == NotificationType.STUDENT_JOINED_CLASSROOM)
            .filter(notification -> notification.getPayload().contains(classroomId.toString()))
            .map(support::inAppNotificationResponse)
            .toList();
    }

    @Transactional
    public void markNotificationRead(UUID classroomId, UUID notificationId) {
        support.requireStudent();
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        ensureStudentEnrolled(classroom);
        notificationService.markAsRead(SecurityUtils.currentUserId(), notificationId);
    }

    @Transactional
    public StudentResultResponse submitResult(UUID classroomId, UUID topicId, SubmitResultRequest request) {
        support.requireStudent();
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        ensureStudentEnrolled(classroom);
        if (classroom.getActivePlan() == null) {
            throw new BusinessRuleViolationException("ACTIVE_PLAN_NOT_FOUND", "The classroom has no active plan");
        }
        PlanTopic activeTopic = planTopicRepository.findByPlanIdAndActiveTrue(classroom.getActivePlan().getId())
            .orElseThrow(() -> new BusinessRuleViolationException("ACTIVE_TOPIC_NOT_FOUND", "The plan does not have an active week"));
        if (!activeTopic.getTopic().getId().equals(topicId) || activeTopic.getWeekNumber() != request.weekNumber()) {
            throw new BusinessRuleViolationException("RESULT_TOPIC_MISMATCH", "The result does not match the active topic or week");
        }
        StudentAttempt attempt = new StudentAttempt();
        attempt.setStudent(currentUser());
        attempt.setClassroom(classroom);
        attempt.setTopic(activeTopic.getTopic());
        attempt.setWeekNumber(request.weekNumber());
        attempt.setScore(request.score());
        attempt.setTimeSpent(request.timeSpent());
        attempt.setCorrectAnswers(request.correctAnswers());
        attempt.setTotalQuestions(request.totalQuestions());
        attempt.setAnswersJson(support.writeJson(request.answers()));
        attempt.setCompletedAt(Instant.now());
        return support.resultResponse(studentAttemptRepository.save(attempt));
    }

    @Transactional(readOnly = true)
    public List<StudentResultWithDetailsResponse> classroomResults(UUID classroomId, Integer weekNumber) {
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        authorizeClassroomView(classroom);
        List<StudentAttempt> attempts = weekNumber != null
            ? studentAttemptRepository.findAllByClassroomIdAndWeekNumberOrderByCompletedAtAsc(classroomId, weekNumber)
            : studentAttemptRepository.findAllByClassroomIdOrderByWeekNumberAscCompletedAtAsc(classroomId);
        return bestAttempts(attempts).stream().map(support::resultWithDetails).toList();
    }

    @Transactional(readOnly = true)
    public List<StudentResultWithDetailsResponse> studentResults(UUID classroomId) {
        support.requireStudent();
        List<StudentAttempt> attempts = classroomId != null
            ? studentAttemptRepository.findAllByStudentIdAndClassroomIdOrderByCompletedAtDesc(SecurityUtils.currentUserId(), classroomId)
            : studentAttemptRepository.findAllByStudentIdOrderByCompletedAtDesc(SecurityUtils.currentUserId());
        return bestAttempts(attempts).stream().map(support::resultWithDetails).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> leaderboard(UUID classroomId, Integer weekNumber) {
        AppClassroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
        authorizeClassroomView(classroom);
        List<StudentAttempt> attempts = weekNumber != null
            ? studentAttemptRepository.findAllByClassroomIdAndWeekNumberOrderByCompletedAtAsc(classroomId, weekNumber)
            : studentAttemptRepository.findAllByClassroomIdOrderByWeekNumberAscCompletedAtAsc(classroomId);
        List<StudentAttempt> best = bestAttempts(attempts);
        best.sort(bestAttemptComparator().reversed());
        List<LeaderboardEntryResponse> rows = new ArrayList<>();
        for (int i = 0; i < best.size(); i++) {
            StudentAttempt attempt = best.get(i);
            rows.add(support.leaderboardEntry(attempt.getStudent(), attempt.getScore(), i + 1));
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public List<HistoryEntryResponse> studentHistory(UUID classroomId, UUID studentId) {
        AppClassroom classroom = ownedClassroom(classroomId);
        if (!enrollmentRepository.existsByClassroomIdAndStudentId(classroomId, studentId)) {
            throw new ResourceNotFoundException("ENROLLMENT_NOT_FOUND", "The student does not belong to the classroom");
        }
        List<StudentAttempt> attempts = studentAttemptRepository.findAllByClassroomIdAndStudentIdOrderByWeekNumberAscCompletedAtAsc(classroomId, studentId);
        return bestAttempts(attempts).stream().map(support::historyEntry).toList();
    }

    @Transactional(readOnly = true)
    public TeacherClassroomDetailResponse teacherClassroomDetail(UUID classroomId) {
        AppClassroom classroom = ownedClassroom(classroomId);
        List<StudentAttempt> attempts = bestAttempts(studentAttemptRepository.findAllByClassroomIdOrderByWeekNumberAscCompletedAtAsc(classroomId));
        return new TeacherClassroomDetailResponse(
            classroomDetails(classroom),
            attempts.stream().map(support::teacherResultRow).toList()
        );
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void activateAutomaticPlanWeeks() {
        LocalDate today = LocalDate.now();
        for (StudyPlan plan : planRepository.findAllByActivationMode(ActivationMode.AUTO)) {
            if (plan.getStartDate() == null || plan.getStartDate().isAfter(today)) {
                continue;
            }
            long weeksElapsed = ChronoUnit.DAYS.between(plan.getStartDate(), today) / 7;
            List<PlanTopic> topics = planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(plan.getId());
            if (topics.isEmpty()) {
                continue;
            }
            int weekToActivate = Math.min((int) weeksElapsed + 1, topics.get(topics.size() - 1).getWeekNumber());
            activateWeekInternal(plan, weekToActivate, false);
        }
    }

    @Scheduled(fixedDelay = 15000L)
    public void expireInactiveClassroomPresence() {
        classroomPresenceStreamService.expireInactiveConnections();
    }

    @Scheduled(fixedDelay = 15000L)
    public void keepAliveClassroomPresenceStreams() {
        classroomPresenceStreamService.publishKeepAlive();
    }

    private void validatePlanRequest(ActivationMode activationMode, LocalDate startDate) {
        if (activationMode == ActivationMode.AUTO && startDate == null) {
            throw new BusinessRuleViolationException("PLAN_START_DATE_REQUIRED", "Automatic plans require startDate");
        }
    }

    private void activateWeekInternal(StudyPlan plan, int weekNumber, boolean allowMissing) {
        List<PlanTopic> topics = planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(plan.getId());
        PlanTopic active = null;
        for (PlanTopic topic : topics) {
            boolean matches = topic.getWeekNumber() == weekNumber;
            topic.setActive(matches);
            if (matches) {
                active = topic;
            }
        }
        if (active == null && !allowMissing) {
            throw new ResourceNotFoundException("PLAN_WEEK_NOT_FOUND", "The requested week does not exist in the plan");
        }
        planTopicRepository.saveAll(topics);
        if (active != null) {
            List<AppClassroom> classrooms = classroomRepository.findAllByActivePlanOrderByCreatedAtDesc(plan);
            classrooms.forEach(classroom -> classroom.setCurrentWeek(weekNumber));
            classroomRepository.saveAll(classrooms);
        }
    }

    private void normalizePlanWeeks(UUID planId) {
        List<PlanTopic> topics = planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(planId);
        for (int i = 0; i < topics.size(); i++) {
            topics.get(i).setWeekNumber(i + 1);
        }
        planTopicRepository.saveAll(topics);
    }

    private User currentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
    }

    private AppClassroom ownedClassroom(UUID classroomId) {
        return classroomRepository.findByIdAndTeacherId(classroomId, SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
    }

    private StudyPlan ownedPlan(UUID planId) {
        return planRepository.findByIdAndTeacherId(planId, SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("PLAN_NOT_FOUND", "Plan not found"));
    }

    private LearningTopic ownedTopic(UUID topicId) {
        return topicRepository.findByIdAndTeacherId(topicId, SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("TOPIC_NOT_FOUND", "Topic not found"));
    }

    private String normalizeCode(String code) {
        String normalized = code == null ? "" : code.trim().toUpperCase();
        if (normalized.length() < 4 || normalized.length() > 10) {
            throw new BusinessRuleViolationException("CLASSROOM_CODE_INVALID", "The code must be between 4 and 10 characters");
        }
        return normalized;
    }

    private ClassroomWithDetailsResponse classroomDetails(AppClassroom classroom) {
        List<User> students = enrolledUsers(classroom);
        StudyPlan plan = classroom.getActivePlan();
        List<PlanTopic> planTopics = plan != null ? planTopicRepository.findAllByPlanIdOrderByWeekNumberAsc(plan.getId()) : List.of();
        return support.classroomWithDetails(classroom, students, plan, planTopics);
    }

    private List<User> enrolledUsers(AppClassroom classroom) {
        return enrollmentRepository.findAllByClassroomOrderByJoinedAtAsc(classroom).stream().map(Enrollment::getStudent).toList();
    }

    private void ensureStudentEnrolled(AppClassroom classroom) {
        if (!enrollmentRepository.existsByClassroomIdAndStudentId(classroom.getId(), SecurityUtils.currentUserId())) {
            throw new UnauthorizedOperationException("FORBIDDEN", "You do not belong to this classroom");
        }
    }

    private void authorizeClassroomView(AppClassroom classroom) {
        if (SecurityUtils.currentUser().role() == UserRole.TEACHER) {
            if (!classroom.getTeacher().getId().equals(SecurityUtils.currentUserId())) {
                throw new UnauthorizedOperationException("FORBIDDEN", "You cannot view this classroom");
            }
            return;
        }
        ensureStudentEnrolled(classroom);
    }

    private void notifyClassroomPeersStudentConnected(AppClassroom classroom, User currentStudent) {
        Instant happenedAt = Instant.now();
        String payload = "{\"classroomId\":\"" + classroom.getId() + "\",\"studentId\":\"" + currentStudent.getId() + "\",\"studentName\":\""
            + escapeJson(currentStudent.getName()) + "\",\"avatarId\":\"" + escapeJson(currentStudent.getAvatarId()) + "\",\"message\":\""
            + escapeJson(currentStudent.getName() + " se conecto al aula") + "\",\"happenedAt\":\"" + happenedAt + "\"}";
        for (User classmate : enrolledUsers(classroom)) {
            if (classmate.getId().equals(currentStudent.getId())) {
                continue;
            }
            notificationService.sendInAppOnce(
                classmate,
                NotificationType.STUDENT_JOINED_CLASSROOM,
                payload,
                java.time.Duration.ofSeconds(45)
            );
        }
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private List<StudentAttempt> bestAttempts(List<StudentAttempt> attempts) {
        Map<String, StudentAttempt> byKey = new LinkedHashMap<>();
        for (StudentAttempt attempt : attempts) {
            String key = attempt.getStudent().getId() + "|" + attempt.getClassroom().getId() + "|" + attempt.getTopic().getId() + "|" + attempt.getWeekNumber();
            StudentAttempt current = byKey.get(key);
            if (current == null || bestAttemptComparator().compare(attempt, current) > 0) {
                byKey.put(key, attempt);
            }
        }
        return new ArrayList<>(byKey.values());
    }

    private StudentAttempt bestAttempt(List<StudentAttempt> attempts) {
        return attempts.stream().max(bestAttemptComparator()).orElse(null);
    }

    private Comparator<StudentAttempt> bestAttemptComparator() {
        return Comparator.comparingInt(StudentAttempt::getScore)
            .thenComparing((StudentAttempt left, StudentAttempt right) -> Integer.compare(right.getTimeSpent(), left.getTimeSpent()))
            .thenComparing((StudentAttempt left, StudentAttempt right) -> right.getCompletedAt().compareTo(left.getCompletedAt()));
    }
}
