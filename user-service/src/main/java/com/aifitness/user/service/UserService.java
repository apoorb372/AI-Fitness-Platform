
package com.aifitness.user.service;

import com.aifitness.user.dto.UserRequest;
import com.aifitness.user.dto.UserResponse;
import com.aifitness.user.entity.User;
import com.aifitness.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A profile already exists for this email"
            );
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setGender(request.getGender().trim());
        user.setAge(request.getAge());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());

        // Keycloak manages authentication.
        // Never copy the Keycloak password into this database.
        user.setPassword(null);

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::new)
                .toList();
    }

    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with id: " + id
                        ));

        return new UserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {

        String normalizedEmail = email.trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User profile not found"
                        ));

        return new UserResponse(user);
    }
}
