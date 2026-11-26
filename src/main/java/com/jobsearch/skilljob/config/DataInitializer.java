package com.jobsearch.skilljob.config;

import com.jobsearch.skilljob.entity.*;
import com.jobsearch.skilljob.enums.*;
import com.jobsearch.skilljob.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Configuration
public class DataInitializer {
	@Bean
	CommandLineRunner seed(UserRepository users, SkillRepository skills, CompanyRepository companies,
			JobRepository jobs, PasswordEncoder encoder,
			@Value("${app.seed.admin-email:admin@skilljobs.local}") String adminEmail,
			@Value("${app.seed.admin-password:Admin@12345}") String adminPassword,
			@Value("${app.seed.enabled:true}") boolean enabled,
@Value("${app.admin.provisioning-enabled:false}") boolean provisionAdmin) {

    return args -> {

        // Create a separate production administrator when requested.
        if (provisionAdmin && users.findByEmail(adminEmail).isEmpty()) {
            users.save(new AppUser(
                    "Portal Administrator",
                    adminEmail,
                    encoder.encode(adminPassword),
                    RoleType.ADMIN
            ));
        }

        // Demo data is controlled independently.
        if (!enabled)
            return;
			if (users.findByEmail(adminEmail).isEmpty())
				users.save(
						new AppUser("Portal Administrator", adminEmail, encoder.encode(adminPassword), RoleType.ADMIN));
			List<String> names = List.of("Java", "Spring Boot", "SQL", "MySQL", "REST API", "Hibernate", "JPA", "HTML",
					"CSS", "JavaScript", "Git", "Docker", "AWS", "Python", "React", "Microservices", "JUnit", "Maven");
			for (String n : names)
				if (skills.findByNameIgnoreCase(n).isEmpty())
					skills.save(new Skill(n));
			if (users.findByEmail("recruiter@skilljobs.local").isEmpty()) {
				AppUser r = users.save(new AppUser("Demo Recruiter", "recruiter@skilljobs.local",
						encoder.encode("Recruiter@123"), RoleType.RECRUITER));
				Company c = companies.save(new Company("TechNova Solutions", r));
				c.setWebsite("https://example.com");
				c.setDescription("Demo technology company for local testing.");
				c.setLocation("Bangalore");
				companies.save(c);
				for (String title : List.of("Java Backend Developer", "Spring Boot Developer",
						"Junior Full Stack Developer")) {
					Job j = new Job(title,
							"Build and maintain production-ready applications. Work with a collaborative engineering team.",
							c);
					j.setLocation("Bangalore");
					j.setSalaryMin(new BigDecimal("400000"));
					j.setSalaryMax(new BigDecimal("800000"));
					j.setExperienceRequired(0);
					j.setJobType(JobType.FULL_TIME);
					j.setWorkMode(WorkMode.HYBRID);
					j.setDeadline(LocalDate.now().plusMonths(2));
					j.getRequiredSkills().add(skills.findByNameIgnoreCase("Java").orElseThrow());
					j.getRequiredSkills().add(skills.findByNameIgnoreCase("Spring Boot").orElseThrow());
					jobs.save(j);
				}
			}
			if (users.findByEmail("candidate@skilljobs.local").isEmpty()) {
				AppUser c = users.save(new AppUser("Demo Candidate", "candidate@skilljobs.local",
						encoder.encode("Candidate@123"), RoleType.CANDIDATE));
				c.getSkills().add(skills.findByNameIgnoreCase("Java").orElseThrow());
				c.getSkills().add(skills.findByNameIgnoreCase("SQL").orElseThrow());
				users.save(c);
			}
		};
	}
}
