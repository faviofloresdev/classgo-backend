package com.classgo.backend.api.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.ActionResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.ClassroomPedagogicalTagInsightsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.CreatePedagogicalTagRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.PedagogicalTagResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.StudentPedagogicalTagInsightsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdatePedagogicalTagRequest;
import com.classgo.backend.application.learning.PedagogicalTagService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PedagogicalTagController {

    private final PedagogicalTagService pedagogicalTagService;

    public PedagogicalTagController(PedagogicalTagService pedagogicalTagService) {
        this.pedagogicalTagService = pedagogicalTagService;
    }

    @PostMapping("/pedagogical-tags")
    public PedagogicalTagResponse create(@Valid @RequestBody CreatePedagogicalTagRequest request) {
        return pedagogicalTagService.create(request);
    }

    @GetMapping("/pedagogical-tags")
    public List<PedagogicalTagResponse> list(@RequestParam(required = false) String query) {
        return pedagogicalTagService.list(query);
    }

    @PatchMapping("/pedagogical-tags/{tagId}")
    public PedagogicalTagResponse update(@PathVariable UUID tagId, @Valid @RequestBody UpdatePedagogicalTagRequest request) {
        return pedagogicalTagService.update(tagId, request);
    }

    @DeleteMapping("/pedagogical-tags/{tagId}")
    public ActionResponse delete(@PathVariable UUID tagId) {
        pedagogicalTagService.delete(tagId);
        return new ActionResponse("Pedagogical tag deleted successfully");
    }

    @GetMapping("/classrooms/{classroomId}/pedagogical-tags/overview")
    public ClassroomPedagogicalTagInsightsResponse classroomInsights(@PathVariable UUID classroomId) {
        return pedagogicalTagService.classroomInsights(classroomId);
    }

    @GetMapping("/classrooms/{classroomId}/students/{studentId}/pedagogical-tags")
    public StudentPedagogicalTagInsightsResponse studentInsights(@PathVariable UUID classroomId, @PathVariable UUID studentId) {
        return pedagogicalTagService.studentInsights(classroomId, studentId);
    }
}
