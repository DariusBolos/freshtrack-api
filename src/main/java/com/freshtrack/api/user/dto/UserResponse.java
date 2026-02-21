package com.freshtrack.api.user.dto;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) { }
