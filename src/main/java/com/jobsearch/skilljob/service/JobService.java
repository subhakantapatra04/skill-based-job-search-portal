package com.jobsearch.skilljob.service;

import com.jobsearch.skilljob.dto.JobDtos.*;
import com.jobsearch.skilljob.entity.*;
import com.jobsearch.skilljob.enums.*;
import com.jobsearch.skilljob.exception.*;
import com.jobsearch.skilljob.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Join;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobService {
	private final JobRepository jobs;
	private final CompanyRepository companies;
	private final SkillRepository skills;
	private final UserRepository users;

	public JobService(JobRepository j, CompanyRepository c, SkillRepository s, UserRepository u) {
		jobs = j;
		companies = c;
		skills = s;
		users = u;
	}

	@Transactional(readOnly = true)
	public Page<JobResponse> search(String q, String location, JobType type, Integer minExp, Pageable pageable,
			String email) {
		AppUser candidate = users.findByEmail(email).orElse(null);
		Set<String> mine = candidate == null ? Set.of()
				: candidate.getSkills().stream().map(x -> x.getName().toLowerCase()).collect(Collectors.toSet());
		Specification<Job> spec = (root, query, cb) -> {
			query.distinct(true);
			var p = cb.equal(root.get("status"), JobStatus.OPEN);
			if (q != null && !q.isBlank()) {
				Join<Job, Skill> sj = root.join("requiredSkills", jakarta.persistence.criteria.JoinType.LEFT);
				p = cb.and(p,
						cb.or(cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%"),
								cb.like(cb.lower(sj.get("name")), "%" + q.toLowerCase() + "%"),
								cb.like(cb.lower(root.join("company").get("name")), "%" + q.toLowerCase() + "%")));
			}
			if (location != null && !location.isBlank())
				p = cb.and(p, cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
			if (type != null)
				p = cb.and(p, cb.equal(root.get("jobType"), type));
			if (minExp != null)
				p = cb.and(p, cb.lessThanOrEqualTo(root.get("experienceRequired"), minExp));
			return p;
		};
		return jobs.findAll(spec, pageable).map(j -> toResponse(j, mine));
	}

	@Transactional(readOnly = true)
	public JobResponse get(Long id, String email) {
		Job j = jobs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
		Set<String> mine = users.findByEmail(email)
				.map(u -> u.getSkills().stream().map(s -> s.getName().toLowerCase()).collect(Collectors.toSet()))
				.orElse(Set.of());
		return toResponse(j, mine);
	}

	@Transactional
	public JobResponse create(JobRequest r, String email) {
		AppUser owner = users.findByEmail(email).orElseThrow();
		Company company = companies.findById(r.companyId())
				.orElseThrow(() -> new ResourceNotFoundException("Company not found"));
		if (!company.getOwner().getId().equals(owner.getId()))
			throw new BadRequestException("You can only post jobs for your own company.");
		Job j = new Job(r.title(), r.description(), company);
		apply(j, r);
		return toResponse(jobs.save(j), Set.of());
	}

	@Transactional
	public JobResponse update(Long id, JobRequest r, String email) {
		Job j = jobs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
		if (!j.getCompany().getOwner().getEmail().equals(email))
			throw new BadRequestException("You do not own this job.");
		apply(j, r);
		return toResponse(j, Set.of());
	}

	private void apply(Job j, JobRequest r) {
		j.setTitle(r.title());
		j.setDescription(r.description());
		j.setLocation(r.location());
		j.setSalaryMin(r.salaryMin());
		j.setSalaryMax(r.salaryMax());
		j.setExperienceRequired(r.experienceRequired());
		j.setJobType(r.jobType());
		j.setWorkMode(r.workMode());
		j.setDeadline(r.deadline());
		j.getRequiredSkills().clear();
		for (Long id : r.skillIds())
			j.getRequiredSkills().add(
					skills.findById(id).orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id)));
	}

	@Transactional
	public void close(Long id, String email) {
		Job j = jobs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
		if (!j.getCompany().getOwner().getEmail().equals(email))
			throw new BadRequestException("You do not own this job.");
		j.setStatus(JobStatus.CLOSED);
	}

	@Transactional(readOnly = true)
	public List<JobResponse> recommended(String email) {
		AppUser u = users.findByEmail(email).orElseThrow();
		Set<String> mine = u.getSkills().stream().map(s -> s.getName().toLowerCase()).collect(Collectors.toSet());
		if (mine.isEmpty())
			return List.of();
		return jobs.findMatchingJobs(JobStatus.OPEN, mine).stream().map(j -> toResponse(j, mine))
				.sorted(Comparator.comparingInt(JobResponse::matchPercentage).reversed()).toList();
	}

	private JobResponse toResponse(Job j, Set<String> mine) {
		Set<String> required = j.getRequiredSkills().stream().map(Skill::getName)
				.collect(Collectors.toCollection(TreeSet::new));
		Set<String> matched = required.stream().filter(s -> mine.contains(s.toLowerCase()))
				.collect(Collectors.toCollection(TreeSet::new));
		Set<String> missing = new TreeSet<>(required);
		missing.removeAll(matched);
		int pct = required.isEmpty() ? 0 : (matched.size() * 100 / required.size());
		return new JobResponse(j.getId(), j.getTitle(), j.getDescription(), j.getLocation(), j.getSalaryMin(),
				j.getSalaryMax(), j.getExperienceRequired(), j.getJobType(), j.getWorkMode(), j.getStatus().name(),
				j.getDeadline(), j.getCompany().getName(), required, pct, matched, missing);
	}
}
