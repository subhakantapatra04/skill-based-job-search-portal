package com.jobsearch.skilljob.controller;

import com.jobsearch.skilljob.dto.ProfileDtos.*;
import com.jobsearch.skilljob.entity.*;
import com.jobsearch.skilljob.repository.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
	private final CompanyRepository companies;
	private final UserRepository users;

	public CompanyController(CompanyRepository c, UserRepository u) {
		companies = c;
		users = u;
	}

	@PostMapping
	public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest r, Authentication a) {
		AppUser owner = users.findByEmail(a.getName()).orElseThrow();
		Company c = new Company(r.name(), owner);
		c.setWebsite(r.website());
		c.setDescription(r.description());
		c.setLocation(r.location());
		c = companies.save(c);
		return ResponseEntity.status(HttpStatus.CREATED).body(dto(c));
	}

	@PutMapping("/{id}")
	public CompanyResponse update(@PathVariable Long id, @Valid @RequestBody CompanyRequest r, Authentication a) {
		AppUser owner = users.findByEmail(a.getName()).orElseThrow();
		Company c = companies.findById(id).orElseThrow();
		if (!c.getOwner().getId().equals(owner.getId()))
			throw new com.jobsearch.skilljob.exception.BadRequestException("You can update only your own company.");
		c.setName(r.name());
		c.setWebsite(r.website());
		c.setDescription(r.description());
		c.setLocation(r.location());
		return dto(companies.save(c));
	}

	@GetMapping("/mine")
	public List<CompanyResponse> mine(Authentication a) {
		AppUser owner = users.findByEmail(a.getName()).orElseThrow();
		return companies.findByOwner_Id(owner.getId()).stream().map(this::dto).toList();
	}

	@GetMapping
	public List<CompanyResponse> all() {
		return companies.findAll().stream().map(this::dto).toList();
	}

	@GetMapping("/{id}")
	public CompanyResponse get(@PathVariable Long id) {
		return dto(companies.findById(id).orElseThrow());
	}

	private CompanyResponse dto(Company c) {
		return new CompanyResponse(c.getId(), c.getName(), c.getWebsite(), c.getDescription(), c.getLocation());
	}
}
