package eu.opertusmundi.message.model;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Notification command object")
@NoArgsConstructor
public class NotificationCommandDto extends BaseMessageCommandDto {

    @Schema(description = "Optional application specific event type")
    @Getter
    @Setter
    private String eventType;
    
    @Schema(description = "Optional application specific data")
    @Getter
    @Setter
    private JsonNode data;
    
}
