package com.company.finflow.security.jwt;

import com.company.finflow.security.auth.FinflowUserDetailsService;
import com.company.finflow.security.auth.FinflowUserPrincipal;
import com.company.finflow.tenant.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final FinflowUserDetailsService userDetailsService;
    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthenticationFilter(
        JwtService jwtService,
        FinflowUserDetailsService userDetailsService,
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/error")
            || path.equals("/api/health")
            || path.startsWith("/api/auth/")
            || path.equals("/actuator/health")
            || path.startsWith("/swagger-ui/")
            || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            exceptionResolver.resolveException(
                request,
                response,
                null,
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token")
            );
            return;
        }

        String token = authorizationHeader.substring(7);
        JwtTokenClaims claims;
        try {
            claims = jwtService.parseToken(token);
        } catch (ResponseStatusException exception) {
            exceptionResolver.resolveException(request, response, null, exception);
            return;
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null || !tenantId.equals(claims.tenantId())) {
            exceptionResolver.resolveException(
                request,
                response,
                null,
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token tenant does not match request tenant")
            );
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            FinflowUserPrincipal principal = userDetailsService.loadByTenantAndEmail(claims.tenantId(), claims.email());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
