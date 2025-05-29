package com.example.keycloak_auth_service.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListUsersResponse {
    private List<UserResponse> users;
    private int currentPage;
    private long totalItems;
    private int totalPages;
}