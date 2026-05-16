package com.freshtrack.api.household.dto;

public record HouseholdMemberResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}

