package com.utvt.ApiSpringCafeSoft.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "GET".equals(request.getMethod()) && "/api/health".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");


        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            boolean valid = jwtUtil.validateToken(token);

            if (valid) {
                try {
                String email     = jwtUtil.extractEmail(token);
                Integer userTipo = jwtUtil.extractUserTipo(token);

                String rol = switch (userTipo == null ? -1 : userTipo) {
                    case 0 -> "ROLE_ADMIN";
                    case 1 -> "ROLE_EMPLEADO";
                    case 4 -> "ROLE_REPARTIDOR";
                    default -> "ROLE_CLIENTE";
                };

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        email, null,
                        List.of(new SimpleGrantedAuthority(rol))
                    );

                SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (io.jsonwebtoken.JwtException | IllegalArgumentException exception) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
