package com.jobsearch.skilljob.controller;

import org.springframework.transaction.annotation.Transactional;
import com.jobsearch.skilljob.entity.*;
import com.jobsearch.skilljob.exception.*;
import com.jobsearch.skilljob.repository.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@Transactional
@RequestMapping("/api/saved-jobs")
public class SavedJobController {
	private final SavedJobRepository saved;
	private final JobRepository jobs;
	private final UserRepository users;

	public SavedJobController(SavedJobRepository s, JobRepository j, UserRepository u) {
		saved = s;
		jobs = j;
		users = u;
	}

	@PostMapping("/{jobId}")
	public ResponseEntity<?> save(@PathVariable Long jobId, Authentication a) {
		AppUser u = users.findByEmail(a.getName()).orElseThrow();
		Job j = jobs.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
		if (saved.existsByUserIdAndJobId(u.getId(), jobId))
			throw new BadRequestException("Job already saved.");
		saved.save(new SavedJob(u, j));
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Job saved"));
	}

	@GetMapping
	public Page<Map<String, Object>> all(Authentication a, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		AppUser u = users.findByEmail(a.getName()).orElseThrow();
		return saved.findByUserId(u.getId(), PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 50)))
				.map(s -> Map.of("jobId", s.getJob().getId(), "title", s.getJob().getTitle(), "company",
						s.getJob().getCompany().getName()));
	}

	@DeleteMapping("/{jobId}")
	public ResponseEntity<Void> delete(@PathVariable Long jobId, Authentication a) {
		AppUser u = users.findByEmail(a.getName()).orElseThrow();
		SavedJob s = saved.findByUserIdAndJobId(u.getId(), jobId)
				.orElseThrow(() -> new ResourceNotFoundException("Saved job not found"));
		saved.delete(s);
		return ResponseEntity.noContent().build();
	}
}
