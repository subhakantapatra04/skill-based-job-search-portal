package com.jobsearch.skilljob.controller;

import com.jobsearch.skilljob.dto.AuthDtos.*;
import com.jobsearch.skilljob.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService service;

	public AuthController(AuthService s) {
		service = s;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.register(r));
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest r) {
		return service.login(r);
	}
}
