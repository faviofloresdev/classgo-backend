package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.NotificationChannel;
import com.classgo.backend.domain.enums.NotificationStatus;
import com.classgo.backend.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;
    @Column(columnDefinition = "text", nullable = false)
    private String payload;
    @Column(name = "sent_at")
    private Instant sentAt;
    @Column(name = "read_at")
    private Instant readAt;
    @Column(name = "error_message")
    private String errorMessage;

    public User getUser() {
        return this.user;
    }

    public String getRecipientEmail() {
        return this.recipientEmail;
    }

    public NotificationType getType() {
        return this.type;
    }

    public NotificationChannel getChannel() {
        return this.channel;
    }

    public NotificationStatus getStatus() {
        return this.status;
    }

    public String getPayload() {
        return this.payload;
    }

    public Instant getSentAt() {
        return this.sentAt;
    }

    public Instant getReadAt() {
        return this.readAt;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setRecipientEmail(final String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public void setType(final NotificationType type) {
        this.type = type;
    }

    public void setChannel(final NotificationChannel channel) {
        this.channel = channel;
    }

    public void setStatus(final NotificationStatus status) {
        this.status = status;
    }

    public void setPayload(final String payload) {
        this.payload = payload;
    }

    public void setSentAt(final Instant sentAt) {
        this.sentAt = sentAt;
    }

    public void setReadAt(final Instant readAt) {
        this.readAt = readAt;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
