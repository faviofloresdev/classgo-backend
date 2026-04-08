package com.classgo.backend.api.challenges;

import com.classgo.backend.api.challenges.dto.ChallengeDtos.ChallengeResponse;
import com.classgo.backend.api.challenges.dto.ChallengeDtos.CreateChallengeRequest;
import com.classgo.backend.application.challenges.ChallengeService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @PostMapping
    public ChallengeResponse create(@Valid @RequestBody CreateChallengeRequest request) {
        return challengeService.create(request);
    }

    @PostMapping("/{id}/publish")
    public ChallengeResponse publish(@PathVariable UUID id) {
        return challengeService.publish(id);
    }

    @GetMapping("/{id}")
    public ChallengeResponse get(@PathVariable UUID id) {
        return challengeService.get(id);
    }

    @GetMapping("/active")
    public ChallengeResponse active(@RequestParam UUID studentId) {
        return challengeService.active(studentId);
    }
}
