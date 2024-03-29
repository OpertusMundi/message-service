package eu.opertusmundi.message.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.message.domain.NotificationEntity;
import eu.opertusmundi.message.model.NotificationCommandDto;
import eu.opertusmundi.message.model.NotificationDto;

@Repository
@Transactional(readOnly = true)
public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, Integer> {

    Optional<NotificationEntity> findOneByKey(UUID key);

    Optional<NotificationEntity> findOneByRecipientAndKey(UUID recipient, UUID key);

    Optional<NotificationEntity> findOneByRecipientAndIdempotentKey(UUID recipient, String idempotentKey);

    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient")
    List<NotificationEntity> findUserNotifications(@Param("recipient") UUID recipient, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient and n.read = false")
    List<NotificationEntity> findUserUnreadNotifications(@Param("recipient") UUID recipient, Pageable pageable);

    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :when WHERE n.recipient = :userKey and n.read = false")
    int readAll(UUID userKey, ZonedDateTime when);

    @Query("SELECT n FROM Notification n WHERE " +
           "   (n.recipient = :userKey) AND " +
           "   (CAST(:dateFrom AS date) IS NULL OR n.sendAt >= :dateFrom) AND " +
           "   (CAST(:dateTo AS date) IS NULL OR n.sendAt <= :dateTo) AND " +
           "   (:read IS NULL OR n.read = :read)")
     Page<NotificationEntity> findAll(
         @Param("userKey") UUID userKey,
         @Param("dateFrom") ZonedDateTime dateFrom,
         @Param("dateTo") ZonedDateTime dateTo,
         @Param("read") Boolean read,
         Pageable pageable
     );

    @Modifying
    @Transactional(readOnly = false)
    @Query("DELETE FROM Notification n WHERE n.recipient = :recipientKey")
    void deleteAllByRecipientKey(UUID recipientKey);

    @Transactional(readOnly = false)
    default NotificationDto send(NotificationCommandDto command) {
        if (!StringUtils.isEmpty(command.getIdempotentKey())) {
            final NotificationEntity existing = this.findOneByRecipientAndIdempotentKey(command.getRecipient(), command.getIdempotentKey()).orElse(null);
            if (existing != null) {
                return existing.toDto();
            }
        }

        final NotificationEntity notification = new NotificationEntity(command.getRecipient());

        notification.setData(command.getData());
        notification.setEventType(command.getEventType());
        notification.setIdempotentKey(command.getIdempotentKey());
        notification.setText(command.getText());

        this.saveAndFlush(notification);

        return notification.toDto();
    }

    @Transactional(readOnly = false)
    default NotificationDto read(UUID recipientKey, UUID key) throws EntityNotFoundException {
        final NotificationEntity notification = this.findOneByRecipientAndKey(recipientKey, key).orElse(null);

        if (notification == null) {
            throw new EntityNotFoundException();
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(ZonedDateTime.now());
        }

        this.saveAndFlush(notification);

        return notification.toDto();
    }

}
