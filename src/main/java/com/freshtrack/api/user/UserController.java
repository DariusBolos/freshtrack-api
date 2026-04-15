package com.freshtrack.api.user;

import com.freshtrack.api.user.dto.UserResponse;
import com.freshtrack.api.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
        User user = userService.getUserByEmail(authHeader.substring(7));

        System.out.println(user);

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
