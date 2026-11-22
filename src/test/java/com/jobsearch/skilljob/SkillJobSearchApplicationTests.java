package com.jobsearch.skilljob;

import com.jobsearch.skilljob.enums.RoleType;
import com.jobsearch.skilljob.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SkillJobSearchApplicationTests {
	@Autowired
	private UserRepository users;

	@Test
	void contextLoads() {
		assertThat(users).isNotNull();
	}

	@Test
	void seedCreatesAdminAndDemoAccounts() {
		assertThat(users.findByEmail("admin@test.local").orElseThrow().getRole()).isEqualTo(RoleType.ADMIN);
		assertThat(users.findByEmail("candidate@skilljobs.local")).isPresent();
		assertThat(users.findByEmail("recruiter@skilljobs.local")).isPresent();
	}
}
