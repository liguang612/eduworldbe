package com.example.eduworldbe.util;

import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Bỏ qua các endpoint Google auth
    String requestPath = request.getRequestURI();
    if (requestPath.startsWith("/api/auth/google/")) {
      filterChain.doFilter(request, response);
      return;
    }

    String authHeader = request.getHeader("Authorization");
    String email = null;
    String jwt = null;

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      jwt = authHeader.substring(7);
      try {
        email = jwtUtil.extractEmail(jwt);
      } catch (Exception e) {
        // Token không hợp lệ hoặc không phải JWT của hệ thống, bỏ qua
        filterChain.doFilter(request, response);
        return;
      }
    }

    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      User user = userRepository.findByEmail(email).orElse(null);
      if (user != null && jwtUtil.isTokenValid(jwt)) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, null);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }
    filterChain.doFilter(request, response);
  }
}
