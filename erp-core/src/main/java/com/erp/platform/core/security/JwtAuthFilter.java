package com.erp.platform.core.security;

import com.erp.platform.core.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            Claims claims = jwtService.extractAllClaims(jwt);

            String email     = claims.getSubject();
            String role      = claims.get("role", String.class);
            String userId    = claims.get("userId", String.class);
            String partnerId = claims.get("partnerId", String.class);
            String partnerCode = claims.get("partnerCode", String.class);
            String defaultLocalId = claims.get("defaultLocalId", String.class);

            if (email == null || role == null) {
                log.debug("JWT missing required claims: email={}, role={}", email, role);
                filterChain.doFilter(request, response);
                return;
            }

            String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityName);

            JwtUserPrincipal principal = JwtUserPrincipal.builder()
                    .email(email)
                    .role(role)
                    .userId(userId)
                    .partnerId(partnerId)
                    .partnerCode(partnerCode)
                    .defaultLocalId(defaultLocalId)
                    .build();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(authority)
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("JWT authenticated: email={}, role={}", email, role);

        } catch (ExpiredJwtException e) {
            log.debug("JWT expired for request: {}", request.getRequestURI());
        } catch (MalformedJwtException e) {
            log.debug("JWT malformed for request: {}", request.getRequestURI());
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/refresh")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
