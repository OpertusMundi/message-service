package eu.opertusmundi.message.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.message.domain.MessageEntity;
import eu.opertusmundi.message.model.MessageCommandDto;
import eu.opertusmundi.message.model.MessageDto;

@Repository
@Transactional(readOnly = true)
public interface JpaMessageRepository extends JpaRepository<MessageEntity, Integer> {

    @Query("SELECT count(m) FROM Message m WHERE m.recipient is null")
    long countUnassignedMessages();

    @Query("SELECT count(m) FROM Message m WHERE m.owner = :owner and m.read = false")
    long countUserNewMessages(@Param("owner") UUID owner);

    Optional<MessageEntity> findOneByOwnerAndKey(UUID owner, UUID key);

    @Query("SELECT m FROM Message m WHERE m.key = :key and m.recipient is null order by m.sendAt desc")
    List<MessageEntity> findAllUnassignedByKey(UUID key);

    @Query("SELECT m FROM Message m WHERE m.owner = :owner and m.thread = :thread order by m.sendAt asc")
    List<MessageEntity> findAllByOwnerAndThread(UUID owner, UUID thread);

    @Query("SELECT m FROM Message m WHERE m.owner = :userKey")
    List<MessageEntity> findUserMessages(@Param("userKey") UUID userKey, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.owner = :userKey and m.read = false")
    List<MessageEntity> findUserUnreadMessages(@Param("userKey") UUID userKey, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.owner = :owner and m.thread = :thread")
    List<MessageEntity> findThreadMessages(@Param("owner") UUID owner, @Param("thread") UUID thread);

    @Query("SELECT m FROM Message m WHERE " +
           "   (m.recipient is null) AND " +
           "   (CAST(:dateFrom AS date) IS NULL OR m.sendAt >= :dateFrom) AND " +
           "   (CAST(:dateTo AS date) IS NULL OR m.sendAt <= :dateTo) AND " +
           "   (:read IS NULL OR m.read = :read)")
    Page<MessageEntity> findHelpdeskUnassignedMessages(
         @Param("dateFrom") ZonedDateTime dateFrom,
         @Param("dateTo") ZonedDateTime dateTo,
         @Param("read") Boolean read,
         Pageable pageable
    );

    @Query("SELECT m FROM Message m WHERE " +
           "   (m.owner = :userKey) AND " +
           "   (CAST(:dateFrom AS date) IS NULL OR m.sendAt >= :dateFrom) AND " +
           "   (CAST(:dateTo AS date) IS NULL OR m.sendAt <= :dateTo) AND " +
           "   (:read IS NULL OR m.read = :read) AND " +
           "   (cast(:contact as org.hibernate.type.UUIDCharType) IS NULL OR m.sender = :contact OR m.recipient = :contact) AND " +
           "   (:thread = false OR m.thread = m.key) ")
    Page<MessageEntity> findUserMessages(
        UUID userKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read, boolean thread, UUID contact, Pageable pageable
    );

    @Transactional(readOnly = false)
    default MessageDto send(MessageCommandDto command) {
        // Unique message key
        final UUID key = UUID.randomUUID();

        // Resolve thread
        final List<MessageEntity> threadMessages = this.findAllByOwnerAndThread(command.getSender(), command.getThread());
        final MessageEntity       lastMessage    = threadMessages.isEmpty() ? null : threadMessages.get(threadMessages.size() - 1);
        final UUID                thread         = lastMessage == null ? key : lastMessage.getThread();

        // Resolve recipient
        if (command.getRecipient() == null && lastMessage != null) {
            // By default reply to the sender of the most recent message in the thread
            UUID recipientKey = lastMessage.getSender();

            if (recipientKey.equals(command.getSender())) {
                // If the last sender is the same user, send the message to the
                // recipient of the last message in the thread
                recipientKey = lastMessage.getRecipient();
            }
            command.setRecipient(recipientKey);
        }
        // Create message for sender
        final MessageEntity senderMessage = new MessageEntity(command.getSender(), key, thread);

        senderMessage.setRecipient(command.getRecipient());
        senderMessage.setSender(command.getSender());
        senderMessage.setSubject(lastMessage == null ? command.getSubject() : lastMessage.getSubject());
        senderMessage.setText(command.getText());
        // Always marked as read for sender
        senderMessage.setRead(true);
        senderMessage.setReadAt(senderMessage.getSendAt());

        this.saveAndFlush(senderMessage);

        // Create message for recipient
        if (command.getRecipient() != null) {
            final MessageEntity recipientMessage = new MessageEntity(command.getRecipient(), key, thread);

            recipientMessage.setRecipient(command.getRecipient());
            recipientMessage.setSender(command.getSender());
            recipientMessage.setSubject(lastMessage == null ? command.getSubject() : lastMessage.getSubject());
            recipientMessage.setText(command.getText());

            this.saveAndFlush(recipientMessage);

            // Update reply
            if (lastMessage != null) {
                lastMessage.setReply(recipientMessage.getKey());
                this.saveAndFlush(lastMessage);
            }
        }

        return senderMessage.toDto();
    }

    @Transactional(readOnly = false)
    default MessageDto read(UUID owner, UUID key) throws EntityNotFoundException {
        final MessageEntity message = this.findOneByOwnerAndKey(owner, key).orElse(null);

        if (message == null) {
            throw new EntityNotFoundException();
        }

        if (!message.isRead()) {
            message.setRead(true);
            message.setReadAt(ZonedDateTime.now());

            this.saveAndFlush(message);
        }

        return message.toDto();
    }

    @Transactional(readOnly = false)
    default MessageDto assignMessage(UUID messageKey, UUID recipientKey) throws EntityNotFoundException {
        // Find message by key
        final List<MessageEntity> unassignedMessages = this.findAllUnassignedByKey(messageKey);

        if (unassignedMessages.isEmpty()) {
            throw new EntityNotFoundException();
        }

        final MessageEntity lastMessage = unassignedMessages.get(0);

        // Update all messages in the thread (user may have send multiple
        // messages)
        final List<MessageEntity> threadMessages = this.findThreadMessages(lastMessage.getOwner(), lastMessage.getThread());

        for (final MessageEntity m : threadMessages) {
            if (m.getRecipient() == null) {
                m.setRecipient(recipientKey);

                this.saveAndFlush(m);

                // Create recipient message
                final MessageEntity recipientMessage = new MessageEntity(recipientKey, m.getKey(), m.getThread());

                recipientMessage.setRecipient(recipientKey);
                recipientMessage.setSender(m.getSender());
                recipientMessage.setSubject(m.getSubject());
                recipientMessage.setText(m.getText());

                this.saveAndFlush(recipientMessage);
            }
        }

        return lastMessage.toDto();
    }

}
