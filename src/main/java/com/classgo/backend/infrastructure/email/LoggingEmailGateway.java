package com.classgo.backend.infrastructure.email;

import org.springframework.stereotype.Component;

@Component
public class LoggingEmailGateway implements EmailGateway {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingEmailGateway.class);

    @Override
    public void send(String recipientEmail, String subject, String htmlBody) {
        log.info("Email queued to {} with subject {}", recipientEmail, subject);
    }
}
