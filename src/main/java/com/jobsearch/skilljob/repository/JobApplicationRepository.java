package com.jobsearch.skilljob.repository;

import com.jobsearch.skilljob.entity.JobApplication;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
	boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

	Page<JobApplication> findByCandidateId(Long candidateId, Pageable pageable);

	Page<JobApplication> findByJobId(Long jobId, Pageable pageable);

	@Query("select a from JobApplication a where a.job.company.owner.id=:ownerId order by a.createdAt desc")
	List<JobApplication> findRecruiterApplications(@Param("ownerId") Long ownerId);

	@Query("select case when count(a)>0 then true else false end from JobApplication a where a.candidate.id=:candidateId and a.job.company.owner.id=:ownerId")
	boolean existsForCandidateAndRecruiter(@Param("candidateId") Long candidateId, @Param("ownerId") Long ownerId);

	long countByStatus(com.jobsearch.skilljob.enums.ApplicationStatus status);
}
