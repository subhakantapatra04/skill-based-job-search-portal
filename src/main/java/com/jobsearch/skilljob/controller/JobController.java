package com.jobsearch.skilljob.controller;

import com.jobsearch.skilljob.dto.JobDtos.*;
import com.jobsearch.skilljob.enums.JobType;
import com.jobsearch.skilljob.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
	private final JobService service;

	public JobController(JobService s) {
		service = s;
	}

	@GetMapping
	public Page<JobResponse> search(@RequestParam(required = false) String q,
			@RequestParam(required = false) String location, @RequestParam(required = false) JobType jobType,
			@RequestParam(required = false) Integer maxExperience, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt,desc") String sort,
			Authentication a) {
		String[] p = sort.split(",");
		Sort.Direction d = p.length > 1 && p[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
		return service.search(q, location, jobType, maxExperience,
				PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 50), Sort.by(d, p[0])),
				a == null ? null : a.getName());
	}

	@GetMapping("/{id}")
	public JobResponse get(@PathVariable Long id, Authentication a) {
		return service.get(id, a == null ? null : a.getName());
	}

	@GetMapping("/recommended")
	public List<JobResponse> recommended(Authentication a) {
		return service.recommended(a.getName());
	}

	@PostMapping
	public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest r, Authentication a) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r, a.getName()));
	}

	@PutMapping("/{id}")
	public JobResponse update(@PathVariable Long id, @Valid @RequestBody JobRequest r, Authentication a) {
		return service.update(id, r, a.getName());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> close(@PathVariable Long id, Authentication a) {
		service.close(id, a.getName());
		return ResponseEntity.noContent().build();
	}
}
