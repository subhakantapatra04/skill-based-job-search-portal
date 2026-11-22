package com.jobsearch.skilljob.config;

import com.jobsearch.skilljob.security.JwtAuthenticationFilter;
import com.jobsearch.skilljob.security.AuthRateLimitFilter;
import org.springframework.context.annotation.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.*;

@Configuration
public class SecurityConfig {
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwt, AuthRateLimitFilter rateLimit)
			throws Exception {
		return http.csrf(c -> c.disable()).cors(c -> c.configurationSource(corsSource()))
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(a -> a
						.requestMatchers("/", "/index.html", "/login.html", "/register.html", "/dashboard.html",
								"/jobs.html", "/css/**", "/js/**", "/uploads/**", "/api/auth/**", "/swagger-ui/**",
								"/swagger-ui.html", "/v3/api-docs/**", "/actuator/health", "/error")
						.permitAll().requestMatchers(HttpMethod.GET, "/api/jobs/recommended").hasRole("CANDIDATE")
						.requestMatchers(HttpMethod.GET, "/api/companies/mine").hasRole("RECRUITER")
						.requestMatchers(HttpMethod.GET, "/api/users/resume/**")
						.hasAnyRole("CANDIDATE", "RECRUITER", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/jobs/**", "/api/skills", "/api/companies/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/skills").hasRole("ADMIN")
						.requestMatchers("/api/admin/**").hasRole("ADMIN").requestMatchers("/api/recruiter/**")
						.hasRole("RECRUITER").requestMatchers(HttpMethod.POST, "/api/companies/**", "/api/jobs/**")
						.hasRole("RECRUITER").requestMatchers(HttpMethod.PUT, "/api/jobs/**", "/api/companies/**")
						.hasRole("RECRUITER").requestMatchers(HttpMethod.DELETE, "/api/jobs/**", "/api/companies/**")
						.hasAnyRole("RECRUITER", "ADMIN").requestMatchers("/api/applications/*/status")
						.hasRole("RECRUITER")
						.requestMatchers("/api/applications/**", "/api/saved-jobs/**", "/api/users/**")
						.hasRole("CANDIDATE").anyRequest().authenticated())
				.exceptionHandling(
						e -> e.authenticationEntryPoint((req, res, ex) -> res.sendError(401, "Unauthorized")))
				.addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(rateLimit, JwtAuthenticationFilter.class).build();
	}

	@Bean
	CorsConfigurationSource corsSource() {
		CorsConfiguration c = new CorsConfiguration();
		c.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
		c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		c.setAllowCredentials(false);
		UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
		s.registerCorsConfiguration("/**", c);
		return s;
	}

	@Bean
	FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
		FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	FilterRegistrationBean<AuthRateLimitFilter> rateLimitFilterRegistration(AuthRateLimitFilter filter) {
		FilterRegistrationBean<AuthRateLimitFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
		return c.getAuthenticationManager();
	}
}
