package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.enums.NotificationChannel;
import com.classgo.backend.domain.enums.NotificationStatus;
import com.classgo.backend.domain.enums.NotificationType;
import com.classgo.backend.domain.model.Notification;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findFirstByUserIdAndTypeAndChannelAndPayloadAndCreatedAtAfterOrderByCreatedAtDesc(
        UUID userId,
        NotificationType type,
        NotificationChannel channel,
        String payload,
        Instant createdAt
    );

    List<Notification> findTop20ByUserIdAndChannelAndStatusOrderByCreatedAtDesc(
        UUID userId,
        NotificationChannel channel,
        NotificationStatus status
    );

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
}
