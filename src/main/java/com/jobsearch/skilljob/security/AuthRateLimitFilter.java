package com.jobsearch.skilljob.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
	private record Window(long startedAt, AtomicInteger count) {
	}

	private final ConcurrentHashMap<String, Window> attempts = new ConcurrentHashMap<>();
	private static final long WINDOW_MS = 60_000;
	private static final int LIMIT = 20;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String p = request.getRequestURI();
		return !p.startsWith("/api/auth/") || !"POST".equalsIgnoreCase(request.getMethod());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		long now = Instant.now().toEpochMilli();
		String key = request.getRemoteAddr();
		Window w = attempts.compute(key, (k,
				old) -> old == null || now - old.startedAt() > WINDOW_MS ? new Window(now, new AtomicInteger(0)) : old);
		if (w.count().incrementAndGet() > LIMIT) {
			response.setStatus(429);
			response.setContentType("application/json");
			response.getWriter().write("{\"message\":\"Too many authentication attempts. Try again in one minute.\"}");
			return;
		}
		if (attempts.size() > 10_000)
			attempts.entrySet().removeIf(e -> now - e.getValue().startedAt() > WINDOW_MS * 2);
		chain.doFilter(request, response);
	}
}
