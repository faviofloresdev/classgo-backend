package com.classgo.backend.infrastructure.security;

import com.classgo.backend.application.auth.AccessTokenRevocationService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ClassGoUserDetailsService userDetailsService;
    private final AccessTokenRevocationService accessTokenRevocationService;

    public JwtAuthenticationFilter(
        JwtService jwtService,
        ClassGoUserDetailsService userDetailsService,
        AccessTokenRevocationService accessTokenRevocationService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.accessTokenRevocationService = accessTokenRevocationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            if (accessTokenRevocationService.isRevoked(token)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            String email = jwtService.extractEmail(token);
            AuthUser userDetails = (AuthUser) userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if ("/api/gameplay/stream".equals(request.getRequestURI())) {
            String queryToken = request.getParameter("accessToken");
            if (StringUtils.hasText(queryToken)) {
                return queryToken;
            }
        }
        return null;
    }
}
