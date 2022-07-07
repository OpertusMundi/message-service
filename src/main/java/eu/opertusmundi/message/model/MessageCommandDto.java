package eu.opertusmundi.message.model;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Message command object")
@NoArgsConstructor
@Getter
@Setter
public class MessageCommandDto extends BaseMessageCommandDto {

    @Schema(description = "Optional application specific idempotent key")
    private String idempotentKey;

    @Schema(description = "Sender unique id", required = true)
    @NotNull
    private UUID sender;

    @Schema(description = "Reply to the specified message thread", required = false)
    private UUID thread;

}
