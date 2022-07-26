package eu.opertusmundi.message.controller.action;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.message.model.BaseResponse;
import eu.opertusmundi.message.model.EnumNotificationSortField;
import eu.opertusmundi.message.model.EnumSortingOrder;
import eu.opertusmundi.message.model.NotificationCommandDto;
import eu.opertusmundi.message.model.NotificationDto;
import eu.opertusmundi.message.model.PageResultDto;
import eu.opertusmundi.message.model.RestResponse;
import eu.opertusmundi.message.service.NotificationService;

@RestController
public class NotificationControllerImpl implements NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Override
    public RestResponse<?> findNotifications(
        Integer pageIndex, Integer pageSize, UUID userKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read,
        EnumNotificationSortField orderBy, EnumSortingOrder order
    ) {
        if (pageIndex == null) {
            pageIndex = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        final PageResultDto<NotificationDto> result = this.notificationService.find(
            pageIndex, pageSize, userKey, dateFrom, dateTo, read, orderBy, order
        );

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> sendNotification(NotificationCommandDto command) {
        final NotificationDto notification = this.notificationService.send(command);

        return RestResponse.result(notification);
    }

    @Override
    public BaseResponse readNotification(UUID recipientKey, UUID key) {
        final NotificationDto notification = this.notificationService.read(recipientKey, key);

        return RestResponse.result(notification);
    }

    @Override
    public BaseResponse readAllNotifications(UUID recipientKey) {
        this.notificationService.readAll(recipientKey);

        return RestResponse.success();
    }

    @Override
    public BaseResponse deleteAllByRecipientKey(UUID recipientKey) {
        this.notificationService.deleteAllByRecipientKey(recipientKey);

        return RestResponse.success();
    }
}
