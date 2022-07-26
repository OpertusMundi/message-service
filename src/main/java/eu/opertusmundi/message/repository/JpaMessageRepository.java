package eu.opertusmundi.message.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.message.domain.MessageEntity;
import eu.opertusmundi.message.domain.MessageThreadEntity;
import eu.opertusmundi.message.model.MessageCommandDto;
import eu.opertusmundi.message.model.MessageDto;
import eu.opertusmundi.message.model.MessageThreadDto;

@Repository
@Transactional(readOnly = true)
public interface JpaMessageRepository extends JpaRepository<MessageThreadEntity, Integer> {

    @Query("SELECT count(m) FROM Message m WHERE m.recipient is null")
    long countUnassignedMessages();

    @Query("SELECT count(m) FROM Message m WHERE m.owner = :owner and m.read = false")
    long countUserNewMessages(@Param("owner") UUID owner);

    @Query("SELECT t FROM MessageThread t WHERE t.owner = :owner and t.key = :key")
    Optional<MessageThreadEntity> findOneThreadByOwnerAndKey(UUID owner, UUID key);

    @Query("SELECT m FROM Message m WHERE m.owner = :owner and m.key = :key")
    Optional<MessageEntity> findOneMessageByOwnerAndKey(UUID owner, UUID key);

    @Query("SELECT m FROM Message m WHERE m.key = :key and m.recipient is null order by m.sendAt desc")
    Optional<MessageEntity> findUnassignedMessageByKey(UUID key);

    @Query("SELECT m FROM Message m WHERE m.owner = :owner and m.thread.key = :thread order by m.sendAt asc")
    List<MessageEntity> findAllMessageByOwnerAndThread(UUID owner, UUID thread);

    @Query("SELECT m FROM Message m WHERE m.owner = :ownerKey")
    List<MessageEntity> findUserMessages(UUID ownerKey, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.owner = :ownerKey and m.read = false")
    List<MessageEntity> findUserUnreadMessages(UUID ownerKey, Pageable pageable);

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
           "   (m.owner = :ownerKey) AND " +
           "   (CAST(:dateFrom AS date) IS NULL OR m.sendAt >= :dateFrom) AND " +
           "   (CAST(:dateTo AS date) IS NULL OR m.sendAt <= :dateTo) AND " +
           "   (:read IS NULL OR m.read = :read) AND " +
           "   (cast(:contact as org.hibernate.type.UUIDCharType) IS NULL OR m.sender = :contact OR m.recipient = :contact) ")
    Page<MessageEntity> findUserMessages(
        UUID ownerKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read, UUID contact, Pageable pageable
    );

    @Query("SELECT t.lastMessage FROM MessageThread t WHERE " +
           "   (t.owner = :ownerKey) AND " +
           "   (CAST(:dateFrom AS date) IS NULL OR t.lastMessage.sendAt >= :dateFrom) AND " +
           "   (CAST(:dateTo AS date) IS NULL OR t.lastMessage.sendAt <= :dateTo) AND " +
           "   (:read IS NULL OR (:read = true AND t.unread = 0) OR (:read = false AND t.unread > 0)) AND " +
           "   (cast(:contact as org.hibernate.type.UUIDCharType) IS NULL OR t.lastMessage.sender = :contact OR t.lastMessage.recipient = :contact) ")
    Page<MessageEntity> findUserThreads(
        UUID ownerKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read, UUID contact, Pageable pageable
    );

    @Modifying
    @Transactional(readOnly = false)
    @Query("DELETE  FROM MessageThread t "
         + "WHERE   t.id in ("
         + "    SELECT DISTINCT m.thread FROM Message m where m.sender = :contactKey or m.recipient = :contactKey"
         + ")")
    void deleteAllByContactKey(UUID contactKey);

    @Transactional(readOnly = false)
    default MessageDto send(MessageCommandDto command) {
        final UUID key = UUID.randomUUID();

        // Resolve thread
        final List<MessageEntity> threadMessages = this.findAllMessageByOwnerAndThread(command.getSender(), command.getThread());
        final MessageEntity       lastMessage    = threadMessages.isEmpty() ? null : threadMessages.get(threadMessages.size() - 1);
        MessageThreadEntity       senderThread   = lastMessage == null ? null : lastMessage.getThread();

        if (senderThread == null) {
            senderThread = new MessageThreadEntity(command.getSender(), key);
            senderThread.setCount(1);
            senderThread.setUnread(0);
        } else {
            senderThread.setCount(senderThread.getCount() + 1);
        }

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
        final MessageEntity senderMessage = new MessageEntity(command.getSender(), key);

        senderMessage.setRecipient(command.getRecipient());
        senderMessage.setSender(command.getSender());
        senderMessage.setSubject(lastMessage == null ? command.getSubject() : lastMessage.getSubject());
        senderMessage.setText(command.getText());
        senderMessage.setThread(senderThread);
        senderMessage.setRead(true);
        senderMessage.setReadAt(senderMessage.getSendAt());

        senderThread.getMessages().add(senderMessage);
        this.saveAndFlush(senderThread);

        this.syncThreadLastMessage(senderThread);

        // Create message for recipient
        if (command.getRecipient() != null) {
            MessageThreadEntity recipientThread = this.findOneThreadByOwnerAndKey(command.getRecipient(), senderThread.getKey()).orElse(null);

            if (recipientThread == null) {
                recipientThread = new MessageThreadEntity(command.getRecipient(), key);
                recipientThread.setCount(1);
                recipientThread.setUnread(1);
            } else {
                recipientThread.setCount(recipientThread.getCount() + 1);
                recipientThread.setUnread(recipientThread.getUnread() + 1);
            }

            final MessageEntity recipientMessage = new MessageEntity(command.getRecipient(), key);

            recipientMessage.setRecipient(command.getRecipient());
            recipientMessage.setSender(command.getSender());
            recipientMessage.setSubject(lastMessage == null ? command.getSubject() : lastMessage.getSubject());
            recipientMessage.setText(command.getText());
            recipientMessage.setThread(recipientThread);

            recipientThread.getMessages().add(recipientMessage);
            this.saveAndFlush(recipientThread);

            this.syncThreadLastMessage(recipientThread);

            // Update reply
            if (lastMessage != null) {
                lastMessage.setReply(recipientMessage.getKey());
                this.saveAndFlush(senderThread);
            }
        }

        return senderMessage.toDto();
    }

    @Transactional(readOnly = false)
    default MessageDto readMessage(UUID ownerKey, UUID messageKey) throws EntityNotFoundException {
        final MessageEntity message = this.findOneMessageByOwnerAndKey(ownerKey, messageKey).orElse(null);
        if (message == null) {
            throw new EntityNotFoundException();
        }
        final MessageThreadEntity thread = message.getThread();

        if (!message.isRead()) {
            message.setRead(true);
            message.setReadAt(ZonedDateTime.now());

            thread.setUnread(thread.getUnread() - 1);
            this.saveAndFlush(thread);
        }

        return message.toDto();
    }

    @Transactional(readOnly = false)
    default MessageThreadDto readThread(UUID ownerKey, UUID threadKey) throws EntityNotFoundException {
        final List<MessageEntity> entities = this.findAllMessageByOwnerAndThread(ownerKey, threadKey);
        final ZonedDateTime       now      = ZonedDateTime.now();

        if (entities.isEmpty()) {
            throw new EntityNotFoundException();
        }
        for (final MessageEntity e : entities) {
            if (!e.isRead()) {
                e.setRead(true);
                e.setReadAt(now);
            }
        }
        final MessageThreadEntity thread = entities.get(0).getThread();
        thread.setUnread(0);

        this.saveAndFlush(thread);

        return thread.toDto(true);
    }

    @Transactional(readOnly = false)
    default MessageDto assignMessage(UUID messageKey, UUID recipientKey) throws EntityNotFoundException {
        // Find message by key
        final MessageEntity lastMessage = this.findUnassignedMessageByKey(messageKey).orElse(null);

        if (lastMessage == null) {
            throw new EntityNotFoundException();
        }

        // Update all messages in the thread (user may have send multiple
        // messages)
        final MessageThreadEntity thread           = lastMessage.getThread();
        MessageThreadEntity       recipientThread  = null;

        thread.getMessages().sort((a, b) -> a.getSendAt().compareTo(b.getSendAt()));

        for (final MessageEntity m : thread.getMessages()) {
            if (m.getRecipient() == null) {
                m.setRecipient(recipientKey);

                // Create recipient thread
                if (recipientThread == null) {
                    recipientThread = new MessageThreadEntity(recipientKey, m.getThread().getKey());
                    recipientThread.setCount(1);
                    recipientThread.setUnread(1);
                } else {
                    recipientThread.setCount(recipientThread.getCount() + 1);
                    recipientThread.setUnread(recipientThread.getUnread() + 1);
                }

                final MessageEntity recipientMessage = new MessageEntity(recipientKey, m.getKey());

                recipientMessage.setRecipient(recipientKey);
                recipientMessage.setSendAt(m.getSendAt());
                recipientMessage.setSender(m.getSender());
                recipientMessage.setSubject(m.getSubject());
                recipientMessage.setText(m.getText());
                recipientMessage.setThread(recipientThread);

                recipientThread.getMessages().add(recipientMessage);
            }
        }

        this.saveAndFlush(recipientThread);
        this.syncThreadLastMessage(recipientThread);

        return lastMessage.toDto();
    }

    default void syncThreadLastMessage(MessageThreadEntity thread) {
        thread.getMessages().sort((a, b) -> a.getSendAt().compareTo(b.getSendAt()));
        thread.setLastMessage(thread.getMessages().get(thread.getMessages().size() - 1));
        this.saveAndFlush(thread);
    }
}
