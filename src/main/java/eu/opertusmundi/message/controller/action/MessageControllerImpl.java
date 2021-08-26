package eu.opertusmundi.message.controller.action;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.message.model.BaseResponse;
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
    public RestResponse<?> findMessages(
        UUID userKey, Integer pageIndex, Integer pageSize, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read
    ) {
        if (pageIndex == null) {
            pageIndex = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        final PageResultDto<MessageDto> result = this.messageService.findUserMessages(pageIndex, pageSize, userKey, dateFrom, dateTo, read);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> sendMessage(MessageCommandDto command) {
        final MessageDto message = this.messageService.send(command);

        return RestResponse.result(message);
    }

    @Override
    public BaseResponse readMessage(UUID owner, UUID key) {
        final MessageDto message = this.messageService.read(owner, key);

        return RestResponse.result(message);
    }

    @Override
    public BaseResponse assignMessage(UUID messageKey, UUID recipientKey) {
        final MessageDto message = this.messageService.assignMessage(messageKey, recipientKey);

        return RestResponse.result(message);
    }

    @Override
    public BaseResponse getMessageThread(UUID threadKey, UUID ownerKey) {
        final List<MessageDto> messages = this.messageService.getMessageThread(threadKey, ownerKey);

        return RestResponse.result(messages);
    }

}