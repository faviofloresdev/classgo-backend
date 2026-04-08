package com.classgo.backend.application.scheduler;

import com.classgo.backend.application.results.ResultService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeeklyChallengeScheduler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WeeklyChallengeScheduler.class);
    private final ResultService resultService;

    public WeeklyChallengeScheduler(ResultService resultService) {
        this.resultService = resultService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void closeChallenges() {
        log.info("Running weekly challenge close scheduler");
        resultService.closeExpiredChallenges();
    }
}
