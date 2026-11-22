package com.jobsearch.skilljob.controller;

import com.jobsearch.skilljob.dto.ProfileDtos.SkillRequest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;

import com.jobsearch.skilljob.entity.Skill;
import com.jobsearch.skilljob.repository.SkillRepository;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
	private final SkillRepository repo;

	public SkillController(SkillRepository r) {
		repo = r;
	}

	@GetMapping
	@Cacheable("skills")
	public List<Map<String, Object>> all() {
		return repo.findAll(Sort.by("name")).stream()
				.map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName())).toList();
	}

	@PostMapping
	@org.springframework.cache.annotation.CacheEvict(value = "skills", allEntries = true)
	public ResponseEntity<?> create(@Valid @RequestBody SkillRequest r) {
		Skill s = repo.findByNameIgnoreCase(r.name()).orElseGet(() -> repo.save(new Skill(r.name().trim())));
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", s.getId(), "name", s.getName()));
	}
}
