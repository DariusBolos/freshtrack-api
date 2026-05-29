package com.freshtrack.api.user;

import com.freshtrack.api.user.dto.ChangePasswordRequest;
import com.freshtrack.api.user.dto.UpdateUserRequest;
import com.freshtrack.api.user.dto.UserResponse;
import com.freshtrack.api.user.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @GetMapping
    public UserResponse getUser(@RequestHeader("Authorization") String authHeader){
        User user = userService.getUserByEmail(extractToken(authHeader));

        System.out.println(user);

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateUserRequest request) {
        User updated = userService.updateUserNames(
                extractToken(authHeader),
                request.firstName(),
                request.lastName()
        );
        return ResponseEntity.ok(new UserResponse(
                updated.getId(),
                updated.getEmail(),
                updated.getFirstName(),
                updated.getLastName()
        ));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(
                extractToken(authHeader),
                request.currentPassword(),
                request.newPassword()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestHeader("Authorization") String authHeader) {
        userService.deleteUserByToken(extractToken(authHeader));
        return ResponseEntity.noContent().build();
    }

    private String extractToken(String authHeader) {
        return authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
    }
}
