package com.jobsearch.skilljob.controller;

import org.springframework.transaction.annotation.Transactional;
import com.jobsearch.skilljob.entity.*;
import com.jobsearch.skilljob.enums.ApplicationStatus;
import com.jobsearch.skilljob.exception.*;
import com.jobsearch.skilljob.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@Transactional
@RequestMapping("/api/recruiter")
public class RecruiterController {
	private final UserRepository users;
	private final CompanyRepository companies;
	private final JobRepository jobs;
	private final JobApplicationRepository apps;

	public RecruiterController(UserRepository u, CompanyRepository c, JobRepository j, JobApplicationRepository a) {
		users = u;
		companies = c;
		jobs = j;
		apps = a;
	}

	@GetMapping("/dashboard")
	public Map<String, Object> dashboard(Authentication a) {
		AppUser u = user(a);
		List<Job> own = jobs.findByCompany_Owner_Id(u.getId());
		List<JobApplication> applications = apps.findRecruiterApplications(u.getId());
		return Map.of("companies", companies.findByOwner_Id(u.getId()).size(), "jobs", own.size(), "openJobs",
				own.stream().filter(j -> j.getStatus().name().equals("OPEN")).count(), "applications",
				applications.size(), "pending",
				applications.stream().filter(x -> x.getStatus() == ApplicationStatus.APPLIED
						|| x.getStatus() == ApplicationStatus.UNDER_REVIEW).count());
	}

	@GetMapping("/jobs")
	public List<Map<String, Object>> myJobs(Authentication a) {
		return jobs.findByCompany_Owner_Id(user(a).getId()).stream().map(j -> Map.<String, Object>of("id", j.getId(),
				"title", j.getTitle(), "company", j.getCompany().getName(), "status", j.getStatus().name(),
				"applications",
				apps.findByJobId(j.getId(), org.springframework.data.domain.Pageable.unpaged()).getTotalElements()))
				.toList();
	}
@GetMapping("/applications")
public List<Map<String, Object>> applications(Authentication a) {
    return apps.findRecruiterApplications(user(a).getId())
            .stream()
            .map(x -> Map.<String, Object>ofEntries(
                    Map.entry("id", x.getId()),
                    Map.entry("candidateId", x.getCandidate().getId()),
                    Map.entry("candidate", x.getCandidate().getFullName()),
                    Map.entry("email", x.getCandidate().getEmail()),
                    Map.entry("resumeUrl",
                            x.getCandidate().getResumeUrl() == null
                                    ? "" : x.getCandidate().getResumeUrl()),
                    Map.entry("jobId", x.getJob().getId()),
                    Map.entry("jobTitle", x.getJob().getTitle()),
                    Map.entry("company", x.getJob().getCompany().getName()),
                    Map.entry("status", x.getStatus().name()),
                    Map.entry("appliedAt", String.valueOf(x.getCreatedAt())),
                    Map.entry("coverLetter",
                            x.getCoverLetter() == null
                                    ? "" : x.getCoverLetter())
            ))
            .toList();
}


	private AppUser user(Authentication a) {
		return users.findByEmail(a.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
