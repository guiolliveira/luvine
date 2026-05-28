package com.javacore.spring_api_luvine.product.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReorderImageRequest(
        @NotNull(message = "Informe a lista de ordenação")
        @NotEmpty(message = "A lista de ordenação não pode ser vazia")
        List<UUID> imagePublicIds
) {
}