package dev.spiffocode.sigesapi.notifications.presentation;

import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record NotificationStatusChangeRequest(
        @Schema(description = "new state for the notification(s)")
        @NotNull
        ReadStatus status
) {
}
