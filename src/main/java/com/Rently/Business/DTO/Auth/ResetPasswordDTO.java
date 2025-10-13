package com.Rently.Business.DTO.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordDTO {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String code;

    @NotBlank @Size(min = 8)
    private String newPassword;

    public ResetPasswordDTO() {}

    public @NotBlank String getCode() {
        return code;
    }

    public void setCode(@NotBlank String code) {
        this.code = code;
    }

    public @NotBlank @Email String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @Email String email) {
        this.email = email;
    }

    public @NotBlank @Size(min = 8) String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(@NotBlank @Size(min = 8) String newPassword) {
        this.newPassword = newPassword;
    }
}

