package com.example.keycloak.auth.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageUrlDTO {
    private UUID previewImageId;
    private UUID fullImageId;
}
