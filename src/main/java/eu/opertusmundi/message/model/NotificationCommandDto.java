package eu.opertusmundi.message.model;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Notification command object")
@NoArgsConstructor
@Getter
@Setter
public class NotificationCommandDto extends BaseMessageCommandDto {

    @Schema(description = "Optional application specific idempotent key")
    private String idempotentKey;

    @Schema(description = "Optional application specific event type")
    private String eventType;

    @Schema(description = "Optional application specific data")
    private JsonNode data;

}
