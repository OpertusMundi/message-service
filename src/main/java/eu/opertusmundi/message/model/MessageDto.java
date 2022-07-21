package eu.opertusmundi.message.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Message object")
@Getter
@Setter
public class MessageDto extends BaseMessageDto {

    public MessageDto() {
        super(EnumMessageType.MESSAGE);
    }

    @Schema(description = "Message unique id")
    private UUID id;

    @Schema(description = "Message thread unique id")
    private UUID thread;

    @Schema(description = "Message sender")
    private UUID sender;

    @Schema(description = "Message recipient")
    private UUID recipient;

    @Schema(description = "Reply message key")
    private UUID reply;

    @Schema(description = "Message subject")
    private String subject;

    @Schema(description = "Number of thread messages. Available only if view is `THREAD_ONLY` or `THREAD_ONLY_UNREAD`")
    @JsonInclude(Include.NON_NULL)
    private Integer threadCount;

    @Schema(description = "Number of thread unread messages. Available only if view is `THREAD_ONLY` or `THREAD_ONLY_UNREAD`")
    @JsonInclude(Include.NON_NULL)
    private Integer threadCountUnread;

}
