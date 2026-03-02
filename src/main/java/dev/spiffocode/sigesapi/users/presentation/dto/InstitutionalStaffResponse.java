package dev.spiffocode.sigesapi.users.presentation.dto;


import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder
@Jacksonized
@Value
@EqualsAndHashCode(callSuper = true)
public class InstitutionalStaffResponse extends ApplicantResponse {
    String employeeNumber;
}
