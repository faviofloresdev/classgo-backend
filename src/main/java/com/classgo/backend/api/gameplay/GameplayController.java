package com.classgo.backend.api.gameplay;

import com.classgo.backend.api.gameplay.dto.GameplayDtos.EndSessionRequest;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.HeartbeatRequest;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.SessionResponse;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.SessionSummaryResponse;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.StartSessionRequest;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.SubmitAnswerRequest;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.SubmitAnswerResponse;
import com.classgo.backend.application.gameplay.GameplayService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameplayController {

    private final GameplayService gameplayService;

    public GameplayController(GameplayService gameplayService) {
        this.gameplayService = gameplayService;
    }

    @PostMapping("/sessions/start")
    public SessionResponse start(@Valid @RequestBody StartSessionRequest request) {
        return gameplayService.start(request);
    }

    @PostMapping("/sessions/{id}/heartbeat")
    public ResponseEntity<Void> heartbeat(@PathVariable UUID id, @Valid @RequestBody HeartbeatRequest request) {
        gameplayService.heartbeat(id, request.timestamp());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/answers")
    public SubmitAnswerResponse answer(@Valid @RequestBody SubmitAnswerRequest request) {
        return gameplayService.answer(request);
    }

    @PostMapping("/sessions/{id}/end")
    public SessionSummaryResponse end(@PathVariable UUID id, @Valid @RequestBody EndSessionRequest request) {
        return gameplayService.end(id, request);
    }
}
