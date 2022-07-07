package eu.opertusmundi.message.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.message.model.NotificationDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Notification")
@Table(name = "`notification`", schema = "messaging")
@TypeDef(
    typeClass      = JsonBinaryType.class,
    defaultForType = JsonNode.class
)
@Getter
@Setter
public class NotificationEntity {

    @Id
    @SequenceGenerator(schema = "messaging", sequenceName = "notification_id_seq", name = "notification_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "notification_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key = UUID.randomUUID();

    @Column(name = "idempotent_key", updatable = false)
    private String idempotentKey;

    @NotNull
    @Column(name = "recipient", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID recipient;

    @NotEmpty
    @Column(name = "`text`")
    private String text;

    @NotNull
    @Column(name = "`send_at`")
    @Setter(AccessLevel.PRIVATE)
    private ZonedDateTime sendAt = ZonedDateTime.now();

    @Column(name = "`read`")
    private boolean read = false;

    @Column(name = "`read_at`")
    private ZonedDateTime readAt;

    @Column(name = "`event_type`")
    private String eventType;

    @Column(name = "`data`", columnDefinition = "jsonb")
    private JsonNode data;

    protected NotificationEntity() {

    }

    public NotificationEntity(UUID recipient) {
        this.recipient = recipient;
    }

    public NotificationDto toDto() {
        final NotificationDto n = new NotificationDto();

        n.setCreatedAt(this.sendAt);
        n.setData(this.data);
        n.setEventType(this.eventType);
        n.setId(this.key);
        n.setIdempotentKey(this.idempotentKey);
        n.setRead(this.read);
        n.setReadAt(this.readAt);
        n.setRecipient(this.recipient);
        n.setText(this.text);

        return n;
    }

}