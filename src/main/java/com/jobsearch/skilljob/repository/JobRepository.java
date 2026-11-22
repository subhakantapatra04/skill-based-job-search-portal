package com.jobsearch.skilljob.repository;

import com.jobsearch.skilljob.entity.Job;
import com.jobsearch.skilljob.enums.JobStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
	@EntityGraph(attributePaths = { "company", "requiredSkills" })
	Page<Job> findByStatus(JobStatus status, Pageable pageable);

	@EntityGraph(attributePaths = { "company", "requiredSkills" })
	@Query("select distinct j from Job j join j.requiredSkills s where j.status=:status and lower(s.name) in :skills")
	List<Job> findMatchingJobs(@Param("status") JobStatus status, @Param("skills") Collection<String> skills);

	List<Job> findByCompany_Owner_Id(Long ownerId);

	long countByStatus(JobStatus status);
}
