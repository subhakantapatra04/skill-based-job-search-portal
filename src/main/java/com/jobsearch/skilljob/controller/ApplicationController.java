package com.jobsearch.skilljob.controller;

import com.jobsearch.skilljob.dto.ProfileDtos.ApplicationResponse;
import com.jobsearch.skilljob.entity.*;
import com.jobsearch.skilljob.enums.*;
import com.jobsearch.skilljob.exception.*;
import com.jobsearch.skilljob.repository.*;
import com.jobsearch.skilljob.service.NotificationService;
import jakarta.validation.constraints.Size;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@Transactional
@RequestMapping("/api/applications")
public class ApplicationController {
	private final JobApplicationRepository apps;
	private final JobRepository jobs;
	private final UserRepository users;
	private final NotificationService notifications;

	public ApplicationController(JobApplicationRepository a, JobRepository j, UserRepository u, NotificationService n) {
		apps = a;
		jobs = j;
		users = u;
		notifications = n;
	}

	@PostMapping("/{jobId}")
	@Transactional
	public ResponseEntity<ApplicationResponse> apply(@PathVariable Long jobId,
			@RequestBody(required = false) Map<String, String> body, Authentication a) {
		AppUser u = user(a);
		if (u.getRole() != RoleType.CANDIDATE)
			throw new BadRequestException("Only candidates can apply.");
		Job j = jobs.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
		if (j.getStatus() != JobStatus.OPEN)
			throw new BadRequestException("This job is not open.");
		if (apps.existsByCandidateIdAndJobId(u.getId(), jobId))
			throw new BadRequestException("You have already applied for this job.");
		JobApplication x = apps.save(new JobApplication(u, j, body == null ? null : body.get("coverLetter")));
		notifications.send(u.getEmail(), "Application submitted: " + j.getTitle(),
				"Your application for " + j.getTitle() + " at " + j.getCompany().getName() + " has been submitted.");
		notifications.send(j.getCompany().getOwner().getEmail(), "New applicant for " + j.getTitle(),
				u.getFullName() + " has applied for your job posting.");
		return ResponseEntity.status(HttpStatus.CREATED).body(dto(x));
	}

	@GetMapping("/my")
	public Page<ApplicationResponse> mine(Authentication a, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return apps.findByCandidateId(user(a).getId(),
				PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 50), Sort.by("createdAt").descending()))
				.map(this::dto);
	}

	@PatchMapping("/{id}/withdraw")
	@Transactional
	public ApplicationResponse withdraw(@PathVariable Long id, Authentication a) {
		JobApplication x = apps.findById(id).orElseThrow(() -> new ResourceNotFoundException("Application not found"));
		if (!x.getCandidate().getEmail().equals(a.getName()))
			throw new BadRequestException("You can withdraw only your own application.");
		if (x.getStatus() == ApplicationStatus.HIRED || x.getStatus() == ApplicationStatus.REJECTED)
			throw new BadRequestException("This application can no longer be withdrawn.");
		x.setStatus(ApplicationStatus.WITHDRAWN);
		return dto(x);
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public ApplicationResponse get(@PathVariable Long id, Authentication a) {
		JobApplication x = apps.findById(id).orElseThrow(() -> new ResourceNotFoundException("Application not found"));
		if (!x.getCandidate().getEmail().equals(a.getName())
				&& !x.getJob().getCompany().getOwner().getEmail().equals(a.getName()))
			throw new BadRequestException("You cannot view this application.");
		return dto(x);
	}

	@PatchMapping("/{id}/status")
	@Transactional
	public ApplicationResponse status(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication a) {
		JobApplication x = apps.findById(id).orElseThrow(() -> new ResourceNotFoundException("Application not found"));
		if (!x.getJob().getCompany().getOwner().getEmail().equals(a.getName()))
			throw new BadRequestException("Only the job owner can update status.");
		x.setStatus(ApplicationStatus.valueOf(body.get("status")));
		notifications.send(x.getCandidate().getEmail(), "Application update: " + x.getJob().getTitle(),
				"Your application status is now " + x.getStatus().name() + ".");
		return dto(x);
	}

	private AppUser user(Authentication a) {
		return users.findByEmail(a.getName()).orElseThrow();
	}

	private ApplicationResponse dto(JobApplication x) {
		return new ApplicationResponse(x.getId(), x.getJob().getId(), x.getJob().getTitle(),
				x.getJob().getCompany().getName(), x.getStatus().name(), String.valueOf(x.getCreatedAt()));
	}
}
