package com.jobsearch.skilljob.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public final class ProfileDtos {
	private ProfileDtos() {
	}

	public record ProfileResponse(Long id, String fullName, String email, String role, String phone, String location,
			String headline, Set<String> skills, String resumeUrl) {
	}

	public record ProfileUpdateRequest(@NotBlank String fullName, String phone, String location, String headline) {
	}

	public record SkillRequest(@NotBlank String name) {
	}

	public record CompanyRequest(@NotBlank String name, String website, String description, String location) {
	}

	public record CompanyResponse(Long id, String name, String website, String description, String location) {
	}

	public record ApplicationResponse(Long id, Long jobId, String jobTitle, String company, String status,
			String appliedAt) {
	}
}
