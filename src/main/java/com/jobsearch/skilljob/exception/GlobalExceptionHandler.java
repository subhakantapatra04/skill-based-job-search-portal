package com.jobsearch.skilljob.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> notFound(RuntimeException e) {
		return response(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler({ BadRequestException.class, IllegalArgumentException.class })
	public ResponseEntity<Map<String, Object>> badRequest(RuntimeException e) {
		return response(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.map(x -> x.getField() + ": " + x.getDefaultMessage()).findFirst().orElse("Invalid request");
		return response(HttpStatus.BAD_REQUEST, msg);
	}

	@ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> conflict(Exception e) {
		return response(HttpStatus.CONFLICT, "The requested record conflicts with existing data.");
	}

	private ResponseEntity<Map<String, Object>> response(HttpStatus s, String m) {
		Map<String, Object> b = new LinkedHashMap<>();
		b.put("timestamp", Instant.now());
		b.put("status", s.value());
		b.put("error", s.getReasonPhrase());
		b.put("message", m);
		return ResponseEntity.status(s).body(b);
	}
}
