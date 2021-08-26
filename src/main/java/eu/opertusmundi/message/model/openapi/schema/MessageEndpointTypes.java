package eu.opertusmundi.message.model.openapi.schema;

import java.util.List;

import eu.opertusmundi.message.model.MessageDto;
import eu.opertusmundi.message.model.PageResultDto;
import eu.opertusmundi.message.model.RestResponse;
import io.swagger.v3.oas.annotations.media.Schema;

public class MessageEndpointTypes {

    @Schema(description = "Message response")
    public static class MessageResponseDto extends RestResponse<MessageDto> {

    }

    @Schema(description = "Message thread response")
    public static class MessageThreadResponseDto extends RestResponse<List<MessageDto>> {

    }

    @Schema(description = "Message query result response")
    public static class MessageListResponseDto extends RestResponse<PageResultDto<MessageDto>> {

    }

}
