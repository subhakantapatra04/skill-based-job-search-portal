package com.jobsearch.skilljob.service;

import com.jobsearch.skilljob.entity.Job;
import com.jobsearch.skilljob.enums.JobStatus;
import com.jobsearch.skilljob.repository.JobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Component
public class JobExpiryScheduler {
	private final JobRepository jobs;

	public JobExpiryScheduler(JobRepository jobs) {
		this.jobs = jobs;
	}

	@Scheduled(cron = "0 15 1 * * *")
	@Transactional
	public void expirePastDeadlineJobs() {
		for (Job job : jobs.findAll()) {
			if (job.getStatus() == JobStatus.OPEN && job.getDeadline() != null
					&& job.getDeadline().isBefore(LocalDate.now()))
				job.setStatus(JobStatus.EXPIRED);
		}
	}
}
