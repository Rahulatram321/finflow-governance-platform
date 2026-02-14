package com.company.finflow.tenant.context;

import com.company.finflow.tenant.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final TenantRepository tenantRepository;
    private final HandlerExceptionResolver exceptionResolver;

    public TenantFilter(
        TenantRepository tenantRepository,
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.tenantRepository = tenantRepository;
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
        try {
            String tenantHeader = request.getHeader(TENANT_HEADER);
            if (!StringUtils.hasText(tenantHeader)) {
                exceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required header: " + TENANT_HEADER)
                );
                return;
            }

            Long tenantId;
            try {
                tenantId = Long.parseLong(tenantHeader);
            } catch (NumberFormatException exception) {
                exceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid tenant id format")
                );
                return;
            }

            if (!tenantRepository.existsById(tenantId)) {
                exceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid tenant id")
                );
                return;
            }

            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
