package com.classgo.backend.api.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.ActivateWeekRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.ActivateWeekResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.AddPlanTopicRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.ActionResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.CreatePlanRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.PlanResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.ReorderPlanTopicsRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdatePlanRequest;
import com.classgo.backend.application.learning.LearningPlatformService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class LearningPlanController {

    private final LearningPlatformService service;

    public LearningPlanController(LearningPlatformService service) {
        this.service = service;
    }

    @PostMapping
    public PlanResponse createPlan(@Valid @RequestBody CreatePlanRequest request) {
        return service.createPlan(request);
    }

    @PatchMapping("/{planId}")
    public PlanResponse updatePlan(@PathVariable UUID planId, @RequestBody UpdatePlanRequest request) {
        return service.updatePlan(planId, request);
    }

    @DeleteMapping("/{planId}")
    public ActionResponse deletePlan(@PathVariable UUID planId) {
        service.deletePlan(planId);
        return new ActionResponse("Plan deleted successfully");
    }

    @GetMapping("/{planId}")
    public PlanResponse plan(@PathVariable UUID planId) {
        return service.plan(planId);
    }

    @PostMapping("/{planId}/topics")
    public PlanResponse addTopicToPlan(@PathVariable UUID planId, @RequestBody AddPlanTopicRequest request) {
        return service.addTopicToPlan(planId, request);
    }

    @DeleteMapping("/{planId}/topics/{topicId}")
    public PlanResponse removeTopicFromPlan(@PathVariable UUID planId, @PathVariable UUID topicId) {
        return service.removeTopicFromPlan(planId, topicId);
    }

    @PutMapping("/{planId}/topics/reorder")
    public PlanResponse reorderPlanTopics(@PathVariable UUID planId, @Valid @RequestBody ReorderPlanTopicsRequest request) {
        return service.reorderPlanTopics(planId, request);
    }

    @PostMapping("/{planId}/activate-week")
    public ActivateWeekResponse activateWeek(@PathVariable UUID planId, @RequestBody ActivateWeekRequest request) {
        return service.activateWeek(planId, request);
    }

    @GetMapping("/teachers/me")
    public List<PlanResponse> teacherPlans() {
        return service.teacherPlans();
    }
}
