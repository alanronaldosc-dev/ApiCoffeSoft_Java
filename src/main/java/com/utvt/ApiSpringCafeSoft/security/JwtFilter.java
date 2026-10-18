
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
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        SecurityContextHolder.clearContext();

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader != null &&
                authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            try {
                boolean valid =
                        jwtUtil.validateToken(token);

                if (valid) {

                    String email =
                            jwtUtil.extractEmail(token);

                    Integer userTipo =
                            jwtUtil.extractUserTipo(token);

                    if (email != null &&
                            !email.isBlank() &&
                            userTipo != null) {

                        String rol = switch (userTipo) {
                            case 0 -> "ROLE_ADMIN";
                            case 1 -> "ROLE_EMPLEADO";
                            case 4 -> "ROLE_REPARTIDOR";
                            default -> "ROLE_CLIENTE";
                        };

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        List.of(
                                                new SimpleGrantedAuthority(rol)
                                        )
                                );

                        SecurityContextHolder
                                .getContext()
                                .setAuthentication(auth);
                    }
                }

            } catch (Exception ex) {

                // No registrar el token ni su contenido.
                SecurityContextHolder.clearContext();

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "No se pudo autenticar el JWT",
                            ex
                    );
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
