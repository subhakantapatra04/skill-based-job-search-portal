package com.jobsearch.skilljob.entity;

import com.jobsearch.skilljob.enums.RoleType;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AppUser extends BaseEntity {
	@Column(nullable = false, length = 100)
	private String fullName;
	@Column(nullable = false, length = 150)
	private String email;
	@Column(nullable = false)
	private String password;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RoleType role;
	private String phone;
	private String location;
	private String headline;
	private String resumeUrl;
	@Column(nullable = false)
	private boolean active = true;
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
	private Set<Skill> skills = new HashSet<>();

	protected AppUser() {
	}

	public AppUser(String fullName, String email, String password, RoleType role) {
		this.fullName = fullName;
		this.email = email;
		this.password = password;
		this.role = role;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String v) {
		fullName = v;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String v) {
		email = v;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String v) {
		password = v;
	}

	public RoleType getRole() {
		return role;
	}

	public void setRole(RoleType v) {
		role = v;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String v) {
		phone = v;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String v) {
		location = v;
	}

	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String v) {
		headline = v;
	}

	public String getResumeUrl() {
		return resumeUrl;
	}

	public void setResumeUrl(String v) {
		resumeUrl = v;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean v) {
		active = v;
	}

	public Set<Skill> getSkills() {
		return skills;
	}
}
