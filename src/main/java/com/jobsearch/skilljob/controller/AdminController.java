package com.jobsearch.skilljob.controller;

import org.springframework.transaction.annotation.Transactional;
import com.jobsearch.skilljob.entity.*;
import com.jobsearch.skilljob.enums.*;
import com.jobsearch.skilljob.exception.*;
import com.jobsearch.skilljob.repository.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@Transactional
@RequestMapping("/api/admin")
public class AdminController {
	private final UserRepository users;
	private final JobRepository jobs;
	private final CompanyRepository companies;
	private final SkillRepository skills;
	private final JobApplicationRepository apps;

	public AdminController(UserRepository u, JobRepository j, CompanyRepository c, SkillRepository s,
			JobApplicationRepository a) {
		users = u;
		jobs = j;
		companies = c;
		skills = s;
		apps = a;
	}

	@GetMapping("/dashboard")
	public Map<String, Object> dashboard() {
		return Map.of("users", users.count(), "candidates",
				users.findAll().stream().filter(u -> u.getRole() == RoleType.CANDIDATE).count(), "recruiters",
				users.findAll().stream().filter(u -> u.getRole() == RoleType.RECRUITER).count(), "companies",
				companies.count(), "jobs", jobs.count(), "openJobs", jobs.countByStatus(JobStatus.OPEN), "applications",
				apps.count(), "skills", skills.count());
	}

	@GetMapping("/users")
	public List<Map<String, Object>> allUsers() {
		return users.findAll().stream()
				.map(u -> Map.<String, Object>of("id", u.getId(), "fullName", u.getFullName(), "email", u.getEmail(),
						"role", u.getRole().name(), "active", u.isActive(), "createdAt",
						String.valueOf(u.getCreatedAt())))
				.toList();
	}

	@PatchMapping("/users/{id}/active")
	public Map<String, Object> active(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
		AppUser u = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		if (u.getRole() == RoleType.ADMIN)
			throw new BadRequestException("Admin accounts cannot be deactivated here.");
		u.setActive(Boolean.TRUE.equals(body.get("active")));
		users.save(u);
		return Map.of("message", "User status updated", "active", u.isActive());
	}

	@GetMapping("/jobs")
	public List<Map<String, Object>> allJobs() {
		return jobs.findAll().stream()
				.map(j -> Map.<String, Object>of("id", j.getId(), "title", j.getTitle(), "company",
						j.getCompany().getName(), "status", j.getStatus().name(), "location",
						j.getLocation() == null ? "" : j.getLocation()))
				.toList();
	}

	@PatchMapping("/jobs/{id}/status")
	public Map<String, Object> jobStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
		Job j = jobs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
		j.setStatus(JobStatus.valueOf(body.get("status")));
		jobs.save(j);
		return Map.of("message", "Job status updated", "status", j.getStatus().name());
	}

	@GetMapping("/companies")
	public List<Map<String, Object>> allCompanies() {
		return companies.findAll().stream()
				.map(c -> Map.<String, Object>of("id", c.getId(), "name", c.getName(), "location",
						c.getLocation() == null ? "" : c.getLocation(), "owner", c.getOwner().getFullName(), "email",
						c.getOwner().getEmail()))
				.toList();
	}

	@GetMapping("/applications")
	public List<Map<String, Object>> allApplications() {
		return apps
				.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
						"createdAt"))
				.stream()
				.map(a -> Map.<String, Object>of("id", a.getId(), "candidate", a.getCandidate().getFullName(), "email",
						a.getCandidate().getEmail(), "job", a.getJob().getTitle(), "company",
						a.getJob().getCompany().getName(), "status", a.getStatus().name()))
				.toList();
	}

	@GetMapping("/skills")
	public List<Map<String, Object>> allSkills() {
		return skills.findAll(org.springframework.data.domain.Sort.by("name")).stream()
				.map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName())).toList();
	}

	@PostMapping("/skills")
	@org.springframework.cache.annotation.CacheEvict(value = "skills", allEntries = true)
	public ResponseEntity<?> addSkill(@RequestBody Map<String, String> body) {
		String name = body.getOrDefault("name", "").trim();
		if (name.isBlank())
			throw new BadRequestException("Skill name is required.");
		Skill s = skills.findByNameIgnoreCase(name).orElseGet(() -> skills.save(new Skill(name)));
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", s.getId(), "name", s.getName()));
	}

	@DeleteMapping("/skills/{id}")
	@org.springframework.cache.annotation.CacheEvict(value = "skills", allEntries = true)
	public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
		Skill s = skills.findById(id).orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
		skills.delete(s);
		return ResponseEntity.noContent().build();
	}
}
