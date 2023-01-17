package eu.opertusmundi.message.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.message.model.MessageDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Message")
@Table(name = "`message`", schema = "messaging")
@TypeDef(
    typeClass      = JsonBinaryType.class,
    defaultForType = ObjectNode.class
)
@Getter
@Setter
public class MessageEntity {

    @Id
    @SequenceGenerator(schema = "messaging", sequenceName = "message_id_seq", name = "message_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "message_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "thread", nullable = false)
    @Getter
    @Setter
    private MessageThreadEntity thread;

    @NotNull
    @Column(name = "owner", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID owner;

    @NotNull
    @Column(name = "sender", updatable = false, columnDefinition = "uuid")
    private UUID sender;

    @Column(name = "recipient", columnDefinition = "uuid")
    private UUID recipient;

    @Column(name = "`subject`")
    private String subject;

    @NotEmpty
    @Column(name = "`text`")
    private String text;

    @NotNull
    @Column(name = "`send_at`")
    private ZonedDateTime sendAt = ZonedDateTime.now();

    @Column(name = "`read`")
    private boolean read = false;


    @Column(name = "`read_at`")
    private ZonedDateTime readAt;

    @Column(name = "`reply`", columnDefinition = "uuid")
    private UUID reply;

    @Column(name = "attributes", columnDefinition = "jsonb")
    private ObjectNode attributes;

    protected MessageEntity() {

    }

    public MessageEntity(UUID owner, UUID key) {
        this.owner  = owner;
        this.key    = key;
    }

    public MessageDto toDto() {
        return this.toDto(false);
    }

    public MessageDto toDto(boolean includeThreadCounts) {
        final MessageDto n = new MessageDto();

        n.setAttributes(attributes);
        n.setCreatedAt(this.sendAt);
        n.setId(this.key);
        n.setSender(this.sender);
        n.setRead(this.read);
        n.setReadAt(this.readAt);
        n.setRecipient(this.recipient);
        n.setReply(this.reply);
        n.setSubject(subject);
        n.setText(this.text);
        n.setThread(this.thread.getKey());

        if (includeThreadCounts) {
            n.setThreadCount(this.thread.getCount());
            n.setThreadCountUnread(this.thread.getUnread());
        }

        return n;
    }

}