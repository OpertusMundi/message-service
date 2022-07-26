package eu.opertusmundi.message.service;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.message.domain.MessageEntity;
import eu.opertusmundi.message.model.EnumMessageView;
import eu.opertusmundi.message.model.MessageCommandDto;
import eu.opertusmundi.message.model.MessageDto;
import eu.opertusmundi.message.model.MessageThreadDto;
import eu.opertusmundi.message.model.PageResultDto;
import eu.opertusmundi.message.repository.JpaMessageRepository;

@Service
@Transactional
public class DefaultMessageService implements MessageService {

    @Autowired
    private JpaMessageRepository messageRepository;

    @Override
    public PageResultDto<MessageDto> findHelpdeskUnassignedMessages(
        Integer pageIndex, Integer pageSize, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read
    ) {
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Direction.DESC, "sendAt"));

        final Page<MessageEntity> page = this.messageRepository.findHelpdeskUnassignedMessages(dateFrom, dateTo, read, pageRequest);

        final PageResultDto<MessageDto> result = PageResultDto.from(page, m -> m.toDto(false));

        return result;
    }

    @Override
    public Long countUnassignedMessages() {
        return this.messageRepository.countUnassignedMessages();
    }

    @Override
    public PageResultDto<MessageDto> findUserMessages(
        Integer pageIndex, Integer pageSize, UUID ownerKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, EnumMessageView view, UUID contactKey
    ) {
        final Boolean             read        = view == EnumMessageView.UNREAD || view == EnumMessageView.THREAD_ONLY_UNREAD ? false : null;
        final boolean             thread      = view == EnumMessageView.THREAD_ONLY || view == EnumMessageView.THREAD_ONLY_UNREAD;
        final PageRequest         pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Direction.DESC, thread ? "lastMessage.sendAt" : "sendAt"));
        final Page<MessageEntity> page        = thread
            ? this.messageRepository.findUserThreads(ownerKey, dateFrom, dateTo, read, contactKey, pageRequest)
            : this.messageRepository.findUserMessages(ownerKey, dateFrom, dateTo, read, contactKey, pageRequest);

        final PageResultDto<MessageDto> result = PageResultDto.from(page, m -> m.toDto(thread));

        return result;
    }

    @Override
    public Long countUserNewMessages(UUID ownerKey) {
        return this.messageRepository.countUserNewMessages(ownerKey);
    }

    @Override
    public MessageDto send(MessageCommandDto command) {
        return this.messageRepository.send(command);
    }

    @Override
    public MessageDto readMessage(UUID ownerKey, UUID messageKey) {
        return this.messageRepository.readMessage(ownerKey, messageKey);
    }

    @Override
    public MessageThreadDto readThread(UUID ownerKey, UUID threadKey) {
        return this.messageRepository.readThread(ownerKey, threadKey);
    }

    @Override
    public MessageDto assignMessage(UUID messageKey, UUID recipientKey) {
        return this.messageRepository.assignMessage(messageKey, recipientKey);
    }

    @Override
    public MessageThreadDto getMessageThread(UUID ownerKey, UUID threadKey) {
        return this.messageRepository.findOneThreadByOwnerAndKey(ownerKey, threadKey)
            .map(t -> t.toDto(true))
            .orElse(null);
    }

    @Override
    public void deleteAllByContactKey(UUID contactKey) {
        this.messageRepository.deleteAllByContactKey(contactKey);
    }
}
