package dev.spiffocode.sigesapi.users.application.service;

import dev.spiffocode.sigesapi.users.presentation.dto.PasswordUpdateRequest;
import dev.spiffocode.sigesapi.users.presentation.dto.RequestAccountRecovery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.net.URI;

public interface PasswordRecoveryService {

    void requestRecovery(@Valid RequestAccountRecovery request);

    URI redirectRecovery(@NotBlank String token);

    void updatePassword(@Valid PasswordUpdateRequest request);
}
