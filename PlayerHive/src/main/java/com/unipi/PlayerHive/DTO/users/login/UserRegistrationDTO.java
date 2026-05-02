package com.unipi.PlayerHive.DTO.users.login;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

public record UserRegistrationDTO (
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank @Email String email,
    @Past LocalDate birthDate
) {}
