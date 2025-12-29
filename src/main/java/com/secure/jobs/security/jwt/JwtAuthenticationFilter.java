package com.secure.jobs.security.jwt;

import com.secure.jobs.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthEntryPoint authEntryPoint;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService, JwtAuthEntryPoint authEntryPoint) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.authEntryPoint = authEntryPoint;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            // If token is present but invalid => respond 401 immediately (no /error forwarding)
            if (!jwtUtils.validateToken(token)) {
                SecurityContextHolder.clearContext();
                authEntryPoint.commence(
                        request,
                        response,
                        new BadCredentialsException("Invalid or expired token")
                );
                return;
            }

            String username = jwtUtils.extractUsernameIfValid(token);

            if (username == null) {
                SecurityContextHolder.clearContext();
                authEntryPoint.commence(
                        request,
                        response,
                        new BadCredentialsException("Invalid or expired token")
                );
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
