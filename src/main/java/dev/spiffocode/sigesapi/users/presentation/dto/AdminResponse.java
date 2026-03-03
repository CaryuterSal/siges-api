package dev.spiffocode.sigesapi.users.presentation.dto;


import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Jacksonized
@Value
public class AdminResponse extends UserResponse{
}
