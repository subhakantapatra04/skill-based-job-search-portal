package com.jobsearch.skilljob.repository;

import com.jobsearch.skilljob.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
	boolean existsByUserIdAndJobId(Long userId, Long jobId);

	Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);

	Page<SavedJob> findByUserId(Long userId, Pageable pageable);
}
