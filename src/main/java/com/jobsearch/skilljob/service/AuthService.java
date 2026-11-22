package com.jobsearch.skilljob.service;

import com.jobsearch.skilljob.dto.AuthDtos.*;
import com.jobsearch.skilljob.entity.AppUser;
import com.jobsearch.skilljob.enums.RoleType;
import com.jobsearch.skilljob.exception.BadRequestException;
import com.jobsearch.skilljob.repository.UserRepository;
import com.jobsearch.skilljob.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private final UserRepository users;
	private final PasswordEncoder encoder;
	private final AuthenticationManager auth;
	private final JwtService jwt;

	public AuthService(UserRepository u, PasswordEncoder e, AuthenticationManager a, JwtService j) {
		users = u;
		encoder = e;
		auth = a;
		jwt = j;
	}

	@Transactional
	public AuthResponse register(RegisterRequest r) {
		if (r.role() == RoleType.ADMIN)
			throw new BadRequestException("Administrator registration is disabled.");
		if (users.existsByEmail(r.email()))
			throw new BadRequestException("Email is already registered.");
		AppUser u = users
				.save(new AppUser(r.fullName(), r.email().toLowerCase(), encoder.encode(r.password()), r.role()));
		return response(u);
	}

	public AuthResponse login(LoginRequest r) {
		auth.authenticate(new UsernamePasswordAuthenticationToken(r.email().toLowerCase(), r.password()));
		AppUser u = users.findByEmail(r.email().toLowerCase()).orElseThrow();
		return response(u);
	}

	private AuthResponse response(AppUser u) {
		return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), "Bearer", u.getId(), u.getFullName(),
				u.getEmail(), u.getRole().name());
	}
}
