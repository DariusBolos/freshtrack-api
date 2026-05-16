package com.freshtrack.api.household.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FamilyInviteRequest(
        @NotBlank(message = "Invite email is required")
        @Email(message = "Invite email must be valid")
        String email,
        String familyName
) {
}

