package org.example.incubyte.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private Boolean active;

    private Long createdAt;
    private Long updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CreateUserRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UpdateUserRequest {
    private String name;
    private String email;
    private String phone;
    private Boolean active;
}