package com.freshtrack.api.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        @JsonAlias("oldPassword")
        String currentPassword,
        @NotBlank(message = "New password is required")
        String newPassword
) {
}
