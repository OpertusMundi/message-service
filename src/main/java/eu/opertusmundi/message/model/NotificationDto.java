package eu.opertusmundi.message.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDto extends BaseMessageDto {

    public NotificationDto() {
        super(EnumMessageType.NOTIFICATION);
    }

    @Schema(description = "Notification unique id")
    private UUID id;

    @Schema(description = "Recipient unique id", required = true)
    private UUID recipient;

    @Schema(description = "Optional application specific event type")
    private String eventType;

    @Schema(description = "Optional application specific data")
    private JsonNode data;

    @Schema(description = "Optional application specific idempotent key")
    @JsonInclude(Include.NON_EMPTY)
    private String idempotentKey;

}
