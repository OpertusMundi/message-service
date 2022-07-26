package eu.opertusmundi.message.service;

import java.time.ZonedDateTime;
import java.util.UUID;

import eu.opertusmundi.message.model.EnumMessageView;
import eu.opertusmundi.message.model.MessageCommandDto;
import eu.opertusmundi.message.model.MessageDto;
import eu.opertusmundi.message.model.MessageThreadDto;
import eu.opertusmundi.message.model.PageResultDto;

public interface MessageService {

    /**
     * Find Helpdesk unassigned messages
     *
     * @param pageIndex
     * @param pageSize
     * @param ownerKey
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
     * @param ownerKey
     * @param dateFrom
     * @param dateTo
     * @param status
     * @param contactKey
     *
     * @return An instance of {@link PageResultDto} with items of type {@link MessageDto}
     */
    PageResultDto<MessageDto> findUserMessages(
        Integer pageIndex, Integer pageSize, UUID ownerKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, EnumMessageView view, UUID contactKey
    );

    /**
     * Count user new messages
     *
     * @param ownerKey
     *
     * @return The number of new (unread) messages
     */
    Long countUserNewMessages(UUID ownerKey);

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
     * @param ownerKey Owner key
     * @param messageKey Message key
     *
     * @return The updated message
     */
    MessageDto readMessage(UUID ownerKey, UUID messageKey);

    /**
     * Mark all messages of a thread as read
     *
     * @param ownerKey Owner key
     * @param messageKey Thread key
     *
     * @return The updated thread
     */
    MessageThreadDto readThread(UUID ownerKey, UUID threadKey);

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
     * @param ownerKey The key of the owner of the specified message thread
     * @param threadKey The key of the message thread
     *
     * @return
     */
    MessageThreadDto getMessageThread(UUID ownerKey, UUID threadKey);

    /**
     * Delete all messages that refer to the specified contact key either as a
     * sender or as a recipient
     *
     * @param contactKey The contact unique key
     */
    void deleteAllByContactKey(UUID contactKey);

}
