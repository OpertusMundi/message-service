package eu.opertusmundi.message.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Message thread object")
@Getter
@Setter
public class MessageThreadDto {

    @Schema(description = "Message thread unique id")
    private UUID key;

    @Schema(description = "Message thread owner")
    private UUID owner;

    @Schema(description = "Message subject")
    private String subject;

    @Schema(description = "Message text")
    private String text;

    @Schema(description = "Modified at")
    private ZonedDateTime modifiedAt;

    @Schema(description = "Number of thread messages")
    private Integer count;

    @Schema(description = "Number of thread unread messages")
    private Integer unread;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Thread messages"
        ),
        minItems = 1
    )
    @JsonInclude(Include.NON_EMPTY)
    private List<MessageDto> messages = new ArrayList<>();
}
