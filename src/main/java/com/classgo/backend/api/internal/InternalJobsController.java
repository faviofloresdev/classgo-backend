package com.classgo.backend.api.internal;

import com.classgo.backend.application.learning.LearningPlatformService;
import com.classgo.backend.application.learning.LearningSupport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/jobs")
public class InternalJobsController {

    private final LearningPlatformService learningPlatformService;
    private final LearningSupport learningSupport;

    public InternalJobsController(LearningPlatformService learningPlatformService, LearningSupport learningSupport) {
        this.learningPlatformService = learningPlatformService;
        this.learningSupport = learningSupport;
    }

    @PostMapping("/activate-plan-weeks")
    public void activatePlanWeeks() {
        learningSupport.requireTeacher();
        learningPlatformService.activateAutomaticPlanWeeks();
    }
}
