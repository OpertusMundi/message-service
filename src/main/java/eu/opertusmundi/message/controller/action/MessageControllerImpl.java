package eu.opertusmundi.message.controller.action;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.message.model.BaseResponse;
import eu.opertusmundi.message.model.EnumMessageStatus;
import eu.opertusmundi.message.model.MessageCommandDto;
import eu.opertusmundi.message.model.MessageDto;
import eu.opertusmundi.message.model.PageResultDto;
import eu.opertusmundi.message.model.RestResponse;
import eu.opertusmundi.message.service.MessageService;

@RestController
public class MessageControllerImpl implements MessageController {

    @Autowired
    private MessageService messageService;

    @Override
    public RestResponse<?> getHelpdeskInbox(
        Integer pageIndex, Integer pageSize, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read
    ) {
        if (pageIndex == null) {
            pageIndex = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        final PageResultDto<MessageDto> result = this.messageService.findHelpdeskUnassignedMessages(pageIndex, pageSize, dateFrom, dateTo, read);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> countUnassignedMessages() {
        final Long result = messageService.countUnassignedMessages();

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> getUserInbox(
        UUID ownerKey, Integer pageIndex, Integer pageSize, ZonedDateTime dateFrom, ZonedDateTime dateTo, EnumMessageStatus status, UUID contactKey
    ) {
        if (pageIndex == null) {
            pageIndex = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        final PageResultDto<MessageDto> result = this.messageService.findUserMessages(pageIndex, pageSize, ownerKey, dateFrom, dateTo, status, contactKey);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> countUserNewMessages(UUID ownerKey) {
        final Long result = messageService.countUserNewMessages(ownerKey);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> sendMessage(MessageCommandDto command) {
        final MessageDto message = this.messageService.send(command);

        return RestResponse.result(message);
    }

    @Override
    public BaseResponse readMessage(UUID ownerKey, UUID messageKey) {
        final MessageDto message = this.messageService.readMessage(ownerKey, messageKey);

        return RestResponse.result(message);
    }

    @Override
    public BaseResponse readThread(UUID ownerKey, UUID threadKey) {
        final List<MessageDto> messages = this.messageService.readThread(ownerKey, threadKey);

        return RestResponse.result(messages);
    }

    @Override
    public BaseResponse assignMessage(UUID messageKey, UUID recipientKey) {
        final MessageDto message = this.messageService.assignMessage(messageKey, recipientKey);

        return RestResponse.result(message);
    }

    @Override
    public BaseResponse getMessageThread(UUID ownerKey, UUID threadKey) {
        final List<MessageDto> messages = this.messageService.getMessageThread(ownerKey, threadKey);

        return RestResponse.result(messages);
    }

}