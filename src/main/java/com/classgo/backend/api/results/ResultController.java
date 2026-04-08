package com.classgo.backend.api.results;

import com.classgo.backend.api.results.dto.ResultDtos.LeaderboardResponse;
import com.classgo.backend.api.results.dto.ResultDtos.ProgressResponse;
import com.classgo.backend.api.results.dto.ResultDtos.WeeklyResultResponse;
import com.classgo.backend.application.results.ResultService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/progress")
    public ProgressResponse progress(@RequestParam UUID studentId, @RequestParam UUID challengeId) {
        return resultService.progress(studentId, challengeId);
    }

    @GetMapping("/leaderboard/{challengeId}")
    public LeaderboardResponse leaderboard(@PathVariable UUID challengeId, @RequestParam(required = false) UUID studentId) {
        return resultService.leaderboard(challengeId, studentId);
    }

    @GetMapping("/results/weekly/{challengeId}")
    public WeeklyResultResponse weeklyResult(@PathVariable UUID challengeId, @RequestParam UUID studentId) {
        return resultService.weeklyResult(challengeId, studentId);
    }
}
