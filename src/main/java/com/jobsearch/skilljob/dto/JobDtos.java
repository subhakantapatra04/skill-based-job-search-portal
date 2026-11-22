package com.jobsearch.skilljob.dto;

import com.jobsearch.skilljob.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public final class JobDtos {
	private JobDtos() {
	}

	public record JobRequest(@NotBlank @Size(max = 180) String title, @NotBlank String description,
			@NotNull Long companyId, String location, @PositiveOrZero BigDecimal salaryMin,
			@PositiveOrZero BigDecimal salaryMax, @PositiveOrZero Integer experienceRequired, @NotNull JobType jobType,
			@NotNull WorkMode workMode, LocalDate deadline, @NotEmpty Set<Long> skillIds) {
	}

	public record JobResponse(Long id, String title, String description, String location, BigDecimal salaryMin,
			BigDecimal salaryMax, Integer experienceRequired, JobType jobType, WorkMode workMode, String status,
			LocalDate deadline, String company, Set<String> requiredSkills, int matchPercentage,
			Set<String> matchedSkills, Set<String> missingSkills) {
	}
}
