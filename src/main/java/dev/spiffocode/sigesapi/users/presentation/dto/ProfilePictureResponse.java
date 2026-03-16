package dev.spiffocode.sigesapi.users.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePictureResponse {
    @Schema(description = "The URL of the uploaded profile picture")
    private String profilePictureUrl;
}
