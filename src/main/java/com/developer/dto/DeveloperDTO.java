package com.developer.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class DeveloperDTO {
    @NotBlank(message = "Имя не может быть пустым!")
    private String fullName;

    @Email(message = "Не корректный email!", regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private String email;
    @NotBlank(message = "Пароль не может быть пустым!")
    private String password;

    private String position;
}
