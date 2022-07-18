package eu.opertusmundi.message.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.opertusmundi.message.domain.MessageEntity;
import eu.opertusmundi.message.model.EnumMessageStatus;
import eu.opertusmundi.message.model.MessageCommandDto;
import eu.opertusmundi.message.model.MessageDto;
import eu.opertusmundi.message.model.PageResultDto;
import eu.opertusmundi.message.repository.JpaMessageRepository;

@Service
public class DefaultMessageService implements MessageService {

    @Autowired
    private JpaMessageRepository messageRepository;

    @Override
    public PageResultDto<MessageDto> findHelpdeskUnassignedMessages(
        Integer pageIndex, Integer pageSize, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read
    ) {
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Direction.DESC, "sendAt"));

        final Page<MessageEntity> page = this.messageRepository.findHelpdeskUnassignedMessages(dateFrom, dateTo, read, pageRequest);

        final PageResultDto<MessageDto> result = PageResultDto.from(page, MessageEntity::toDto);

        return result;
    }

    @Override
    public Long countUnassignedMessages() {
        return this.messageRepository.countUnassignedMessages();
    }

    @Override
    public PageResultDto<MessageDto> findUserMessages(
        Integer pageIndex, Integer pageSize, UUID userKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, EnumMessageStatus status, UUID contactKey
    ) {
        final PageRequest         pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Direction.DESC, "sendAt"));
        final Boolean             read        = status == EnumMessageStatus.UNREAD || status == EnumMessageStatus.THREAD_ONLY_UNREAD ? false : null;
        final boolean             thread      = status == EnumMessageStatus.THREAD_ONLY || status == EnumMessageStatus.THREAD_ONLY_UNREAD;
        final Page<MessageEntity> page        = this.messageRepository.findUserMessages(userKey, dateFrom, dateTo, read, thread, contactKey, pageRequest);

        final PageResultDto<MessageDto> result = PageResultDto.from(page, MessageEntity::toDto);

        return result;
    }

    @Override
    public Long countUserNewMessages(UUID userKey) {
        return this.messageRepository.countUserNewMessages(userKey);
    }

    @Override
    public MessageDto send(MessageCommandDto command) {
        return this.messageRepository.send(command);
    }

    @Override
    public MessageDto read(UUID owner, UUID key) {
        return this.messageRepository.read(owner, key);
    }

    @Override
    public MessageDto assignMessage(UUID messageKey, UUID recipientKey) {
        return this.messageRepository.assignMessage(messageKey, recipientKey);
    }

    @Override
    public List<MessageDto> getMessageThread(UUID threadKey, UUID ownerKey) {
        return this.messageRepository.findAllByOwnerAndThread(ownerKey, threadKey).stream()
            .map(MessageEntity::toDto)
            .collect(Collectors.toList());
    }
}
