package com.freshtrack.api.household.dto;

public record FamilyInviteResponse(
        String inviteId,
        String familyName,
        String email
) {
}

