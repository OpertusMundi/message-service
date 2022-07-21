package eu.opertusmundi.message.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.message.model.MessageThreadDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "MessageThread")
@Table(name = "`message_thread`", schema = "messaging")
@Getter
@Setter
public class MessageThreadEntity {

    @Id
    @SequenceGenerator(schema = "messaging", sequenceName = "message_thread_id_seq", name = "message_thread_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "message_thread_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @Version
    @Column(name = "row_version")
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private long rowVersion;

    @NotNull
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key;

    @NotNull
    @Column(name = "owner", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID owner;

    @OneToMany(
        mappedBy = "thread",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Setter(AccessLevel.PRIVATE)
    private List<MessageEntity> messages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "last_message")
    @Getter
    @Setter
    private MessageEntity lastMessage;

    @NotNull
    @Column(name = "`count`")
    private Integer count = 0;

    @NotNull
    @Column(name = "`count_unread`")
    private Integer unread = 0;

    protected MessageThreadEntity() {

    }

    public MessageThreadEntity(UUID owner, UUID key) {
        this.owner = owner;
        this.key   = key;
    }

    public MessageThreadDto toDto() {
        return this.toDto(false);
    }

    public MessageThreadDto toDto(boolean includeMessages) {
        final MessageThreadDto t = new MessageThreadDto();
        final MessageEntity    m = this.getLastMessage();

        t.setCount(count);
        t.setKey(key);
        t.setModifiedAt(m.getSendAt());
        t.setOwner(owner);
        t.setSubject(m.getSubject());
        t.setText(m.getText());
        t.setUnread(unread);

        if (includeMessages) {
            this.getMessages().stream()
                .map(MessageEntity::toDto)
                .forEach(t.getMessages()::add);

            this.getMessages().sort((a, b) -> a.getSendAt().compareTo(b.getSendAt()));
        }

        return t;
    }

}