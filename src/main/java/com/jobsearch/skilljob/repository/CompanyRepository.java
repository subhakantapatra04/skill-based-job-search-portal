package com.jobsearch.skilljob.repository;

import com.jobsearch.skilljob.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
	List<Company> findByOwner_Id(Long ownerId);
}
