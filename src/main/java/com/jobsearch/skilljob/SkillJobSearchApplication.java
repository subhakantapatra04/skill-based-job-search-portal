package com.jobsearch.skilljob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@EnableCaching
public class SkillJobSearchApplication {
	public static void main(String[] args) {
		SpringApplication.run(SkillJobSearchApplication.class, args);
	}
}
