package eu.opertusmundi.message.service;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.opertusmundi.message.domain.NotificationEntity;
import eu.opertusmundi.message.model.EnumNotificationSortField;
import eu.opertusmundi.message.model.EnumSortingOrder;
import eu.opertusmundi.message.model.NotificationCommandDto;
import eu.opertusmundi.message.model.NotificationDto;
import eu.opertusmundi.message.model.PageResultDto;
import eu.opertusmundi.message.repository.JpaNotificationRepository;

@Service
public class DefaultNotificationService implements NotificationService {

    @Autowired
    private JpaNotificationRepository notificationRepository;

    @Override
    public PageResultDto<NotificationDto> find(
        Integer pageIndex, Integer pageSize,
        UUID userKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read,
        EnumNotificationSortField orderBy, EnumSortingOrder order
    ) {
        final Direction   direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));

        final Page<NotificationEntity> page = this.notificationRepository.findAll(userKey, dateFrom, dateTo, read, pageRequest);

        final PageResultDto<NotificationDto> result = PageResultDto.from(page, NotificationEntity::toDto);

        return result;
    }

    @Override
    public NotificationDto send(NotificationCommandDto command) {
        return this.notificationRepository.send(command);
    }

    @Override
    public NotificationDto read(UUID recipientKey, UUID key) {
        return this.notificationRepository.read(recipientKey, key);
    }

    @Override
    public void readAll(UUID recipientKey) {
        this.notificationRepository.readAll(recipientKey, ZonedDateTime.now());
    }

}
