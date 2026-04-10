package com.classgo.backend.application.notifications;

import com.classgo.backend.domain.enums.NotificationChannel;
import com.classgo.backend.domain.enums.NotificationStatus;
import com.classgo.backend.domain.enums.NotificationType;
import com.classgo.backend.domain.model.Notification;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.repository.NotificationRepository;
import com.classgo.backend.infrastructure.email.EmailGateway;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final EmailGateway emailGateway;

    public NotificationService(NotificationRepository notificationRepository, EmailGateway emailGateway) {
        this.notificationRepository = notificationRepository;
        this.emailGateway = emailGateway;
    }

    @Transactional
    public void send(User user, String recipientEmail, NotificationType type, String subject, String htmlBody, String payload) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setRecipientEmail(recipientEmail);
        notification.setType(type);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setPayload(payload);
        notification = notificationRepository.save(notification);
        try {
            emailGateway.send(recipientEmail, subject, htmlBody);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
        } catch (Exception ex) {
            log.error("Email send failed", ex);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(ex.getMessage());
        }
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendInApp(User user, NotificationType type, String payload) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setRecipientEmail(user.getEmail());
        notification.setType(type);
        notification.setChannel(NotificationChannel.IN_APP);
        notification.setStatus(NotificationStatus.SENT);
        notification.setPayload(payload);
        notification.setSentAt(Instant.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public boolean sendInAppOnce(User user, NotificationType type, String payload, Duration dedupeWindow) {
        Instant threshold = Instant.now().minus(dedupeWindow);
        boolean exists = notificationRepository.findFirstByUserIdAndTypeAndChannelAndPayloadAndCreatedAtAfterOrderByCreatedAtDesc(
            user.getId(),
            type,
            NotificationChannel.IN_APP,
            payload,
            threshold
        ).isPresent();
        if (exists) {
            return false;
        }
        sendInApp(user, type, payload);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Notification> inAppNotifications(UUID userId) {
        return notificationRepository.findTop20ByUserIdAndChannelAndStatusOrderByCreatedAtDesc(
            userId,
            NotificationChannel.IN_APP,
            NotificationStatus.SENT
        );
    }

    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("NOTIFICATION_NOT_FOUND", "Notification not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
    }
}
