package eu.opertusmundi.message.controller.action;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.message.model.BaseResponse;
import eu.opertusmundi.message.model.EnumMessageStatus;
import eu.opertusmundi.message.model.MessageCommandDto;
import eu.opertusmundi.message.model.RestResponse;
import eu.opertusmundi.message.model.openapi.schema.MessageEndpointTypes;
import eu.opertusmundi.message.model.openapi.schema.MessageEndpointTypes.MessageResponseDto;
import eu.opertusmundi.message.model.openapi.schema.MessageEndpointTypes.MessageThreadResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Messaging endpoint
 */
@Tag(
    name        = "Message",
    description = "The message API"
)
@RequestMapping(path = "/v1/messages", produces = MediaType.APPLICATION_JSON_VALUE)
public interface MessageController {

    /**
     * Get Helpdesk unassigned messages
     *
     * @param pageIndex
     * @param pageSize
     * @param dateFrom
     * @param dateTo
     * @param read
     *
     * @return An instance of {@link MessageEndpointTypes.MessageListResponseDto}
     */
    @Operation(
        summary     = "Get Helpdesk Inbox",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = MessageEndpointTypes.MessageListResponseDto.class)
        )
    )
    @GetMapping(value = "/helpdesk")
    @Secured({"ROLE_USER"})
    RestResponse<?> getHelpdeskInbox(
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Page index",
            schema      = @Schema(type = "integer", defaultValue = "0")
        )
        @RequestParam(name = "page", required = false) Integer pageIndex,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Page size",
            schema      = @Schema(type = "integer", defaultValue = "10")
        )
        @RequestParam(name = "size", required = false) Integer pageSize,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter messages after date"
        )
        @RequestParam(name = "date-from", required = false) ZonedDateTime dateFrom,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter messages before date"
        )
        @RequestParam(name = "date-to", required = false) ZonedDateTime dateTo,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter read messages"
        )
        @RequestParam(name = "read", required = false) Boolean read
    );

    /**
     * Count Helpdesk unassigned messages
     *
     * @return An instance of {@link MessageEndpointTypes.CountResponseDto}
     */
    @Operation(
        summary     = "Get Helpdesk Inbox count",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = MessageEndpointTypes.CountResponseDto.class)
        )
    )
    @GetMapping(value = "/helpdesk/count")
    @Secured({"ROLE_USER"})
    RestResponse<?> countUnassignedMessages();

    /**
     * Find messages
     *
     * @param ownerKey
     * @param pageIndex
     * @param pageSize
     * @param dateFrom
     * @param dateTo
     * @param status
     * @param contactKey
     *
     * @return An instance of {@link MessageEndpointTypes.MessageListResponseDto}
     */
    @Operation(
        summary     = "Find messages",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = MessageEndpointTypes.MessageListResponseDto.class)
        )
    )
    @GetMapping(value = "/user/{ownerKey}")
    @Secured({"ROLE_USER"})
    RestResponse<?> getUserInbox(
        @Parameter(
            in          = ParameterIn.PATH,
            description = "The key of the message owner"
        )
        @PathVariable(name = "ownerKey") UUID ownerKey,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Page index",
            schema      = @Schema(type = "integer", defaultValue = "0")
        )
        @RequestParam(name = "page", required = false) Integer pageIndex,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Page size",
            schema      = @Schema(type = "integer", defaultValue = "10")
        )
        @RequestParam(name = "size", required = false) Integer pageSize,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter messages after date"
        )
        @RequestParam(name = "date-from", required = false) ZonedDateTime dateFrom,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter messages before date"
        )
        @RequestParam(name = "date-to", required = false) ZonedDateTime dateTo,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter messages by status"
        )
        @RequestParam(name = "status", required = false) EnumMessageStatus status,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter messages by contract key"
        )
        @RequestParam(name = "contactKey", required = false) UUID contactKey
    );

    /**
     * Count user new messages
     *
     * @param ownerKey
     * @return An instance of {@link MessageEndpointTypes.CountResponseDto}
     */
    @Operation(
        summary     = "Find messages",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = MessageEndpointTypes.CountResponseDto.class)
        )
    )
    @GetMapping(value = "/user/{ownerKey}/count")
    @Secured({"ROLE_USER"})
    RestResponse<?> countUserNewMessages(
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Filter user by key"
        )
        @PathVariable(name = "ownerKey") UUID ownerKey
    );

    /**
     * Send message
     *
     * @param message Message configuration object
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Send a message",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MessageResponseDto.class))
    )
    @PostMapping(value = "")
    @Secured({"ROLE_USER"})
    RestResponse<?> sendMessage(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Message command object",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MessageCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) MessageCommandDto message
    );

    /**
     * Mark message as read
     *
     * @param ownerKey The owner of the message
     * @param messageKey The message key
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Read message",
        description = "Marks a message as read",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/user/{ownerKey}/message/{messageKey}")
    @Secured({"ROLE_USER"})
    BaseResponse readMessage(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Message owner's unique key"
        )
        @PathVariable(name = "ownerKey", required = true) UUID ownerKey,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Message unique key"
        )
        @PathVariable(name = "messageKey", required = true) UUID messageKey
    );

    /**
     * Mark all messages of a thread as read
     *
     * @param ownerKey The owner of the thread
     * @param threadKey The thread key
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Read thread",
        description = "Marks all messages of a thread as read",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/user/{ownerKey}/thread/{threadKey}")
    @Secured({"ROLE_USER"})
    BaseResponse readThread(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Thread owner's unique key"
        )
        @PathVariable(name = "ownerKey", required = true) UUID ownerKey,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "The thread unique key"
        )
        @PathVariable(name = "threadKey", required = true) UUID threadKey
    );

    /**
     * Assign message to Helpdesk user
     *
     * @param key The message to mark as read
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Assign message",
        description = "Assigns a message to Helpdesk user",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/{messageKey}/recipient/{recipientKey}")
    @Secured({"ROLE_USER"})
    BaseResponse assignMessage(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Message unique key"
        )
        @PathVariable(name = "messageKey", required = true) UUID messageKey,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Recipient unique key"
        )
        @PathVariable(name = "recipientKey", required = true) UUID recipientKey
    );

    /**
     * Get a message thread
     *
     * @param ownerKey The owner of the message
     * @param messageKey The key of any message from the thread
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Get message thread",
        description = "Get all messages of the thread that the specified message key belongs to",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MessageThreadResponseDto.class))
    )
    @GetMapping(value = "/user/{ownerKey}/thread/{threadKey}")
    @Secured({"ROLE_USER"})
    BaseResponse getMessageThread(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Thread owner unique key"
        )
        @PathVariable(name = "ownerKey") UUID ownerKey,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Message thread unique key"
        )
        @PathVariable(name = "threadKey") UUID threadKey
    );

}
