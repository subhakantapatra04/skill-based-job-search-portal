package com.jobsearch.skilljob.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwt;
	private final CustomUserDetailsService users;

	public JwtAuthenticationFilter(JwtService jwt, CustomUserDetailsService users) {
		this.jwt = jwt;
		this.users = users;
	}

	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		String h = req.getHeader("Authorization");
		if (h != null && h.startsWith("Bearer ")) {
			String token = h.substring(7);
			if (jwt.isValid(token)) {
				String email = jwt.extractEmail(token);
				if (SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails d = users.loadUserByUsername(email);
					if (!d.isEnabled()) {
						chain.doFilter(req, res);
						return;
					}
					var a = new UsernamePasswordAuthenticationToken(d, null, d.getAuthorities());
					a.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
					SecurityContextHolder.getContext().setAuthentication(a);
				}
			}
		}
		chain.doFilter(req, res);
	}
}
