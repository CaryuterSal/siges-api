package dev.spiffocode.sigesapi.users.application.service;

import lombok.Builder;

import java.util.List;

@Builder
public record UserFilter(
        String searchQuery,
        String createdBy,
        List<UserTypeFilter> userTypes,
        ShowModeFilter showMode
) {
}
