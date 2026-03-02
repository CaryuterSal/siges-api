package dev.spiffocode.sigesapi.users.presentation.dto;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@Builder
@Jacksonized
@Value
public class AdminResponse extends UserResponse{
}
