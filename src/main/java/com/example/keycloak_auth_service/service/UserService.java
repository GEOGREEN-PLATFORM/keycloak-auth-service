package com.example.keycloak_auth_service.service;

import com.example.keycloak_auth_service.model.dto.ListUsersResponse;
import com.example.keycloak_auth_service.model.dto.UserRequest;
import com.example.keycloak_auth_service.model.dto.UserResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface UserService {
    UserResponse updateUser(String token, String email, UserRequest userRequest);

    UserResponse getUserByEmail(String email);

    UserResponse getUserById(UUID id);

    ListUsersResponse getUsers(int page, int size, String search, String role, String status,
                               LocalDate fromDate, LocalDate toDate);
}
