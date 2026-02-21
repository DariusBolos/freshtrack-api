package com.freshtrack.api.auth.service;


import com.freshtrack.api.auth.dto.AuthenticationResponse;
import com.freshtrack.api.auth.dto.LoginRequest;
import com.freshtrack.api.auth.dto.RegistrationRequest;

public interface IAuthService {
    AuthenticationResponse register(RegistrationRequest request);
    AuthenticationResponse login(LoginRequest request);
}
