package com.jobsearch.skilljob.dto;

import jakarta.validation.constraints.*;
import com.jobsearch.skilljob.enums.RoleType;

public final class AuthDtos {
	private AuthDtos() {
	}

	public record RegisterRequest(@NotBlank @Size(max = 100) String fullName, @NotBlank @Email String email,
			@NotBlank @Size(min = 8, max = 72) String password, @NotNull RoleType role) {
	}

	public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
	}

	public record AuthResponse(String token, String tokenType, Long userId, String fullName, String email,
			String role) {
	}
}
