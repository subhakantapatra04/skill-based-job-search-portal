package com.jobsearch.skilljob.entity;

import com.jobsearch.skilljob.enums.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "jobs", indexes = { @Index(name = "idx_job_location", columnList = "location"),
		@Index(name = "idx_job_status_created", columnList = "status,createdAt") })
public class Job extends BaseEntity {
	@Column(nullable = false, length = 180)
	private String title;
	@Column(nullable = false, length = 5000)
	private String description;
	private String location;
	private BigDecimal salaryMin;
	private BigDecimal salaryMax;
	private Integer experienceRequired;
	@Enumerated(EnumType.STRING)
	private JobType jobType;
	@Enumerated(EnumType.STRING)
	private WorkMode workMode;
	@Enumerated(EnumType.STRING)
	private JobStatus status = JobStatus.OPEN;
	private LocalDate deadline;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id")
	private Company company;
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
	private Set<Skill> requiredSkills = new HashSet<>();
	@Version
	private Long version;

	protected Job() {
	}

	public Job(String title, String description, Company company) {
		this.title = title;
		this.description = description;
		this.company = company;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String v) {
		title = v;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String v) {
		description = v;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String v) {
		location = v;
	}

	public BigDecimal getSalaryMin() {
		return salaryMin;
	}

	public void setSalaryMin(BigDecimal v) {
		salaryMin = v;
	}

	public BigDecimal getSalaryMax() {
		return salaryMax;
	}

	public void setSalaryMax(BigDecimal v) {
		salaryMax = v;
	}

	public Integer getExperienceRequired() {
		return experienceRequired;
	}

	public void setExperienceRequired(Integer v) {
		experienceRequired = v;
	}

	public JobType getJobType() {
		return jobType;
	}

	public void setJobType(JobType v) {
		jobType = v;
	}

	public WorkMode getWorkMode() {
		return workMode;
	}

	public void setWorkMode(WorkMode v) {
		workMode = v;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus v) {
		status = v;
	}

	public LocalDate getDeadline() {
		return deadline;
	}

	public void setDeadline(LocalDate v) {
		deadline = v;
	}

	public Company getCompany() {
		return company;
	}

	public Set<Skill> getRequiredSkills() {
		return requiredSkills;
	}
}
