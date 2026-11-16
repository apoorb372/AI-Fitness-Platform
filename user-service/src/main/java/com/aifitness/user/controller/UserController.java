
package com.aifitness.user.controller;

import com.aifitness.user.dto.UserRequest;
import com.aifitness.user.dto.UserResponse;
import com.aifitness.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> createUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserRequest request) {

        String authenticatedEmail = jwt.getClaimAsString("email");

        if (authenticatedEmail == null ||
                !authenticatedEmail.equalsIgnoreCase(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserResponse response = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @AuthenticationPrincipal Jwt jwt) {

        String email = jwt.getClaimAsString("email");

        return ResponseEntity.ok(
                userService.getAllUsers().stream()
                        .filter(user -> user.getEmail().equalsIgnoreCase(email))
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        UserResponse user = userService.getUserById(id);
        String email = jwt.getClaimAsString("email");

        if (email == null || !email.equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @PathVariable String email,
            @AuthenticationPrincipal Jwt jwt) {

        String authenticatedEmail = jwt.getClaimAsString("email");

        if (authenticatedEmail == null ||
                !authenticatedEmail.equalsIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.getUserByEmail(email));
    }
}
