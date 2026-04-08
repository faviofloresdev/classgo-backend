package com.classgo.backend.infrastructure.email;

public interface EmailGateway {
    void send(String recipientEmail, String subject, String htmlBody);
}
