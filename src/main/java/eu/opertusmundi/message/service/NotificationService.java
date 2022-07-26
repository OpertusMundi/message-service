package eu.opertusmundi.message.service;

import java.time.ZonedDateTime;
import java.util.UUID;

import eu.opertusmundi.message.model.EnumNotificationSortField;
import eu.opertusmundi.message.model.EnumSortingOrder;
import eu.opertusmundi.message.model.NotificationCommandDto;
import eu.opertusmundi.message.model.NotificationDto;
import eu.opertusmundi.message.model.PageResultDto;

public interface NotificationService {

    /**
     * Find notifications
     *
     * @param pageIndex
     * @param pageSize
     * @param userKey
     * @param dateFrom
     * @param dateTo
     * @param read
     * @param orderBy
     * @param order
     *
     * @return An instance of {@link PageResultDto} with items of type {@link NotificationDto}
     */
    PageResultDto<NotificationDto> find(
        Integer pageIndex, Integer pageSize,
        UUID userKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read,
        EnumNotificationSortField orderBy, EnumSortingOrder order
    );

    /**
     * Send a new notification
     *
     * @param command The notification command
     * @return The new notification
     */
    NotificationDto send(NotificationCommandDto command);

    /**
     * Mark a notification as read
     *
     * @param recipientKey Recipient key
     * @param key Notification key
     *
     * @return The updated notification
     */
    NotificationDto read(UUID recipientKey, UUID key);

    /**
     * Mark all notifications as read
     *
     * @param userKey
     */
    void readAll(UUID recipientKey);

    /**
     * Delete all notifications for the specified recipient
     *
     * @param recipientKey The recipient unique key
     */
    void deleteAllByRecipientKey(UUID recipientKey);
}
