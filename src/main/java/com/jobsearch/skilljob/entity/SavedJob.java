package com.jobsearch.skilljob.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "saved_jobs", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "job_id" }))
public class SavedJob extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private AppUser user;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_id")
	private Job job;

	protected SavedJob() {
	}

	public SavedJob(AppUser u, Job j) {
		user = u;
		job = j;
	}

	public AppUser getUser() {
		return user;
	}

	public Job getJob() {
		return job;
	}
}
