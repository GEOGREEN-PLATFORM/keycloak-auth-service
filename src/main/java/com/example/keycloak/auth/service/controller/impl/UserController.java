package com.example.keycloak.auth.service.controller.impl;

import com.example.keycloak.auth.service.model.dto.ListUsersResponse;
import com.example.keycloak.auth.service.model.dto.UserRequest;
import com.example.keycloak.auth.service.model.dto.UserResponse;
import com.example.keycloak.auth.service.service.UserServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

import static com.example.keycloak.auth.service.util.AuthorizationStringUtil.ADMIN;
import static com.example.keycloak.auth.service.util.AuthorizationStringUtil.AUTHORIZATION;
import static com.example.keycloak.auth.service.util.AuthorizationStringUtil.OPERATOR;
import static com.example.keycloak.auth.service.util.AuthorizationStringUtil.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/search")
@SecurityRequirement(name = AUTHORIZATION)
public class UserController {
    private final UserServiceImpl userService;

    @RolesAllowed({ADMIN, OPERATOR, USER})
    @PatchMapping("/{email}")
    public ResponseEntity<UserResponse> updateUser(@RequestHeader(AUTHORIZATION) String token,
                                                   @PathVariable("email") String email,
                                                   @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(token, email, request));
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @RolesAllowed({ADMIN, OPERATOR, USER})
    @GetMapping(params = {"page", "size"})
    public ResponseEntity<ListUsersResponse> getUsers(@NotNull @RequestParam("page") int page,
                                                      @NotNull @RequestParam("size") int size,
                                                      @RequestParam(value = "search", required = false) String search,
                                                      @RequestParam(value = "role", required = false) String role,
                                                      @RequestParam(value = "status", required = false) String status,
                                                      @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                      @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(userService.getUsers(page, size, search, role, status, fromDate, toDate));
    }
}