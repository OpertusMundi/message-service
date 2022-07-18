package eu.opertusmundi.message.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.message.model.EnumMessageStatus;
import eu.opertusmundi.message.model.MessageCommandDto;
import eu.opertusmundi.message.model.MessageDto;
import eu.opertusmundi.message.model.PageResultDto;

public interface MessageService {

    /**
     * Find Helpdesk unassigned messages
     *
     * @param pageIndex
     * @param pageSize
     * @param userKey
     * @param dateFrom
     * @param dateTo
     * @param read
     *
     * @return An instance of {@link PageResultDto} with items of type {@link MessageDto}
     */
    PageResultDto<MessageDto> findHelpdeskUnassignedMessages(
        Integer pageIndex, Integer pageSize, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read
    );

    /**
     * Count Helpdesk unassigned messages
     *
     * @return The number of messages
     */
    Long countUnassignedMessages();

    /**
     * Find messages
     *
     * @param pageIndex
     * @param pageSize
     * @param userKey
     * @param dateFrom
     * @param dateTo
     * @param status
     * @param contactKey
     *
     * @return An instance of {@link PageResultDto} with items of type {@link MessageDto}
     */
    PageResultDto<MessageDto> findUserMessages(
        Integer pageIndex, Integer pageSize, UUID userKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, EnumMessageStatus read, UUID contactKey
    );

    /**
     * Count user new messages
     *
     * @param userKey
     *
     * @return The number of new (unread) messages
     */
    Long countUserNewMessages(UUID userKey);

    /**
     * Send a new message
     *
     * @param command The message command
     * @return The new message
     */
    MessageDto send(MessageCommandDto command);

    /**
     * Mark a message as read
     *
     * @param owner Owner key
     * @param key Message key
     *
     * @return The updated message
     */
    MessageDto read(UUID owner, UUID key);

    /**
     * Assign message to Helpdesk user
     *
     * @param messageKey Message key
     * @param recipientKey Helpdesk account key
     *
     * @return The updated message
     */
    MessageDto assignMessage(UUID messageKey, UUID recipientKey);

    /**
     * Get message thread
     *
     * @param messageKey The key of the message thread
     * @param ownerKey The key of the owner of the specified message thread
     *
     * @return
     */
    List<MessageDto> getMessageThread(UUID threadKey, UUID ownerKey);

}
