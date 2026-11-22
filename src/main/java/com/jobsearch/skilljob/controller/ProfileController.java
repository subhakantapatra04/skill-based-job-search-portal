package com.jobsearch.skilljob.controller;

import com.jobsearch.skilljob.dto.ProfileDtos.*;
import com.jobsearch.skilljob.entity.*;
import com.jobsearch.skilljob.exception.*;
import com.jobsearch.skilljob.repository.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class ProfileController {
	private final UserRepository users;
	private final SkillRepository skills;

	public ProfileController(UserRepository u, SkillRepository s) {
		users = u;
		skills = s;
	}

	@GetMapping("/profile")
	@Transactional(readOnly = true)
	public ProfileResponse profile(Authentication a) {
		AppUser u = user(a);
		return new ProfileResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(), u.getPhone(),
				u.getLocation(), u.getHeadline(),
				u.getSkills().stream().map(Skill::getName).collect(Collectors.toSet()), u.getResumeUrl());
	}

	@PutMapping("/profile")
	@Transactional
	public ProfileResponse update(@Valid @RequestBody ProfileUpdateRequest r, Authentication a) {
		AppUser u = user(a);
		u.setFullName(r.fullName());
		u.setPhone(r.phone());
		u.setLocation(r.location());
		u.setHeadline(r.headline());
		users.save(u);
		return profile(a);
	}

	@PostMapping("/skills/{skillId}")
	@Transactional
	public ProfileResponse addSkill(@PathVariable Long skillId, Authentication a) {
		AppUser u = user(a);
		u.getSkills().add(skills.findById(skillId).orElseThrow(() -> new ResourceNotFoundException("Skill not found")));
		users.save(u);
		return profile(a);
	}

	@DeleteMapping("/skills/{skillId}")
	@Transactional
	public ProfileResponse removeSkill(@PathVariable Long skillId, Authentication a) {
		AppUser u = user(a);
		u.getSkills().removeIf(s -> s.getId().equals(skillId));
		users.save(u);
		return profile(a);
	}

	private AppUser user(Authentication a) {
		return users.findByEmail(a.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
