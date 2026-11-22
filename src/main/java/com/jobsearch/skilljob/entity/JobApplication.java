package com.jobsearch.skilljob.entity;

import com.jobsearch.skilljob.enums.ApplicationStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "job_applications", uniqueConstraints = @UniqueConstraint(columnNames = { "candidate_id", "job_id" }))
public class JobApplication extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "candidate_id")
	private AppUser candidate;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_id")
	private Job job;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApplicationStatus status = ApplicationStatus.APPLIED;
	@Column(length = 2000)
	private String coverLetter;

	protected JobApplication() {
	}

	public JobApplication(AppUser c, Job j, String cover) {
		candidate = c;
		job = j;
		coverLetter = cover;
	}

	public AppUser getCandidate() {
		return candidate;
	}

	public Job getJob() {
		return job;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public void setStatus(ApplicationStatus s) {
		status = s;
	}

	public String getCoverLetter() {
		return coverLetter;
	}
}
