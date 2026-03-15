package dev.spiffocode.sigesapi.notifications.infrastructure.web;

import dev.spiffocode.sigesapi.notifications.application.service.NotificationFilter;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationsService;
import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import dev.spiffocode.sigesapi.notifications.presentation.NotificationResponse;
import dev.spiffocode.sigesapi.notifications.presentation.NotificationStatusChangeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationsController {

        private final NotificationsService service;

        @GetMapping
        @Operation(summary = "gets all notifications for the logged in user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "successful", useReturnTypeSchema = true),
        })
        @PageableAsQueryParam
        public Page<NotificationResponse> listNotifications(
                        @SortDefault(sort = "sentAt") @ParameterObject @Schema(description = "Paging and ordering info") Pageable pageable,
                        @RequestParam(required = false) @Schema(description = "Filter by notifications read and unread") ReadStatus status,
                        @RequestParam(required = false) @Schema(description = "Filter by type of notification (semantic purpose)") Type type) {
                NotificationFilter filter = NotificationFilter.builder()
                                .readStatus(status)
                                .type(type)
                                .build();

                return service.listNotifications(pageable, filter);
        }

        @PatchMapping("{id}/status")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "successful", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "404", description = "not found")
        })
        @Operation(summary = "changes a single notification status to READ or UNREAD")
        public NotificationResponse changeNotificationStatus(@PathVariable Long id,
                        @RequestBody @Valid NotificationStatusChangeRequest request) {
                return service.changeNotificationStatus(id, request.status());
        }

        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "successful", useReturnTypeSchema = true)
        })
        @PatchMapping("/status")
        @Operation(summary = "changes all notifications status from user to READ or UNREAD")
        public ResponseEntity<@NonNull Void> changeAllNotificationsStatus(
                        @RequestBody @Valid NotificationStatusChangeRequest request) {
                service.changeAllNotificationsStatus(request.status());
                return ResponseEntity.noContent().build();
        }

}
