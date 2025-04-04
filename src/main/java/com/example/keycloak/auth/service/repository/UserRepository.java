package com.example.keycloak.auth.service.repository;

import com.example.keycloak.auth.service.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
}
