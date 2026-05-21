package com.javacore.spring_api_luvine.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(

        @NotBlank(message = "Informe o nome do destinátario")
        @Size(min = 2, max = 100, message = "Nome do destinátario deve ter entre 2 a 100 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$",
                message = "Nome do destinátario não pode ter números ou caracteres especiais")
        String firstName,

        @NotBlank(message = "Informe o sobrenome do destinátario")
        @Size(min = 2, max = 100, message = "Sobrenome do destinátario deve ter entre 2 a 100 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$",
                message = "Sobrenome do destinátario não pode ter números ou caracteres especiais")
        String lastName,

        @NotBlank(message = "Informe o CEP")
        @Size(min = 8, max = 8, message = "O CEP deve ter exatamente 8 digitos")
        String cep,

        @NotBlank(message = "Informe o endereço")
        @Size(max = 255, message = "O endereço é muito longo")
        String street,

        @NotBlank(message = "Informe o número do imóvel")
        @Size(max = 10, message = "O número do imóvel deve ter no máximo 10 caracteres")
        String number,

        @Size(max = 100, message = "O complemento deve ter no máximo 100 caracteres")
        String complement,

        @NotBlank(message = "Informe o bairro")
        @Size(max = 100, message = "O nome do bairro deve ter no máximo 100 caracteres")
        String neighborhood,

        @NotBlank(message = "Informe a cidade")
        @Size(max = 100, message = "O nome da cidade deve ter no máximo 100 caracteres")
        String city,

        @NotBlank(message = "Informe o estado")
        @Size(max = 20, message = "O nome do estado deve conter no máximo 20 caracteres")
        String state,

        @NotBlank(message = "Informe o país")
        @Size(max = 100, message = "O nome do país deve conter no máximo 100 caracteres")
        String country,

        @NotBlank(message = "Informe o número de telefone")
        String phone,

        boolean defaultAddress
) {
}