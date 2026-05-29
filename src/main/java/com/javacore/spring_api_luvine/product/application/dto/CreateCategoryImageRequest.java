package com.javacore.spring_api_luvine.product.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryImageRequest(

        @NotBlank(message = "Informe o texto alternativo")
        @Size(min = 3, max = 150, message = "O texto alternativo deve ter entre 3 a 150 caracteres")
        String altText,

        @Min(value = 1, message = "A ordem de exibição das imagens deve começar no minimo em 1")
        @Max(value = 10, message = "A ordem de exibição das imagens deve ser até no máximo 10")
        Integer displayOrder
) {
}