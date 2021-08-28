package eu.opertusmundi.message.model;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Message object")
public class MessageDto extends BaseMessageDto {

    public MessageDto() {
        super(EnumMessageType.MESSAGE);
    }

    @Schema(description = "Message unique id")
    @Getter
    @Setter
    private UUID id;

    @Schema(description = "Message thread unique id")
    @Getter
    @Setter
    private UUID thread;

    @Schema(description = "Message sender")
    @Getter
    @Setter
    private UUID sender;

    @Schema(description = "Message recipient")
    @Getter
    @Setter
    private UUID recipient;

    @Schema(description = "Reply message key")
    @Getter
    @Setter
    private UUID reply;

}
