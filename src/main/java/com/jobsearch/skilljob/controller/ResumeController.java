package com.jobsearch.skilljob.controller;

import com.jobsearch.skilljob.entity.AppUser;
import com.jobsearch.skilljob.enums.RoleType;
import com.jobsearch.skilljob.exception.*;
import com.jobsearch.skilljob.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/users/resume")
public class ResumeController {
	private final UserRepository users;
	private final JobApplicationRepository applications;
	@Value("${app.upload-dir:uploads}")
	private String uploadDir;

	public ResumeController(UserRepository u, JobApplicationRepository a) {
		users = u;
		applications = a;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Map<String, String> upload(@RequestParam("file") MultipartFile file, Authentication auth) throws Exception {
		if (file.isEmpty())
			throw new BadRequestException("Choose a resume file.");
		String original = Objects.requireNonNullElse(file.getOriginalFilename(), "resume");
		String lower = original.toLowerCase(Locale.ROOT);
		if (!(lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx")))
			throw new BadRequestException("Only PDF, DOC or DOCX files are allowed.");
		if (file.getSize() > 5 * 1024 * 1024)
			throw new BadRequestException("Maximum resume size is 5 MB.");
		AppUser user = users.findByEmail(auth.getName()).orElseThrow();
		Path dir = Paths.get(uploadDir, "resumes").toAbsolutePath().normalize();
		Files.createDirectories(dir);
		String ext = lower.substring(lower.lastIndexOf('.'));
		Path target = dir.resolve(filename(user.getId()) + ext);
		file.transferTo(target);
		String url = "/api/users/resume/" + user.getId();
		user.setResumeUrl(url);
		users.save(user);
		return Map.of("message", "Resume uploaded", "resumeUrl", url);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<Resource> download(@PathVariable Long userId, Authentication auth) throws Exception {
		AppUser requester = users.findByEmail(auth.getName()).orElseThrow();
		AppUser candidate = users.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
		boolean allowed = requester.getId().equals(userId) || requester.getRole() == RoleType.ADMIN
				|| (requester.getRole() == RoleType.RECRUITER
						&& applications.existsForCandidateAndRecruiter(userId, requester.getId()));
		if (!allowed)
			throw new BadRequestException("You are not allowed to view this resume.");
		Path dir = Paths.get(uploadDir, "resumes").toAbsolutePath().normalize();
		Path file = null;
		for (String ext : List.of(".pdf", ".docx", ".doc")) {
			Path candidateFile = dir.resolve(filename(userId) + ext);
			if (Files.exists(candidateFile)) {
				file = candidateFile;
				break;
			}
		}
		if (file == null)
			throw new ResourceNotFoundException("Resume not uploaded");
		Resource resource = new FileSystemResource(file);
		String type = Files.probeContentType(file);
		if (type == null)
			type = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(type))
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
				.body(resource);
	}

	private String filename(Long id) {
		return "candidate-" + id;
	}
}
