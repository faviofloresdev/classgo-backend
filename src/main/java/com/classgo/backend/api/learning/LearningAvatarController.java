package com.classgo.backend.api.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.AvatarResponse;
import com.classgo.backend.application.learning.LearningPlatformService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avatars")
public class LearningAvatarController {

    private final LearningPlatformService service;

    public LearningAvatarController(LearningPlatformService service) {
        this.service = service;
    }

    @GetMapping
    public List<AvatarResponse> avatars() {
        return service.avatars();
    }
}
