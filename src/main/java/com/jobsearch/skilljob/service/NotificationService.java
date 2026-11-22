package com.jobsearch.skilljob.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
	private final JavaMailSender mail;
	@Value("${app.mail.enabled:false}")
	private boolean enabled;
	@Value("${app.mail.from:no-reply@skilljobs.local}")
	private String from;

	public NotificationService(JavaMailSender mail) {
		this.mail = mail;
	}

	@Async
	public void send(String to, String subject, String body) {
		if (!enabled || to == null || to.isBlank())
			return;
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			mail.send(message);
		} catch (Exception ex) {
			System.err.println("Email notification failed: " + ex.getMessage());
		}
	}
}
