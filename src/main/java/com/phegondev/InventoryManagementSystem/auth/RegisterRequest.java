package com.phegondev.InventoryManagementSystem.auth;

import com.phegondev.InventoryManagementSystem.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Email is required")
    private String email;

    /**
     * 8-72 chars (BCrypt only considers the first 72 bytes), at least one letter
     * and one digit. Enforced again in UserServiceImpl for the admin-update path.
     */
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
            message = "Password must be at least 8 characters and contain both letters and numbers")
    private String password;

    @NotBlank(message = "PhoneNumber is required")
    private String phoneNumber;
    private UserRole role;

}
