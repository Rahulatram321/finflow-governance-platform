package com.company.finflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.ArrayList;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";
    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final Set<String> PAGINATED_LIST_PATHS = Set.of(
        "/api/expenses",
        "/api/workflow-steps",
        "/api/budgets",
        "/api/audit-logs",
        "/api/users"
    );

    @Bean
    public OpenAPI finflowOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("FINFLOW API")
                .version("v1")
                .description("Financial Workflow Automation Platform APIs"))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
            .components(new Components().addSecuritySchemes(
                BEARER_SCHEME,
                new SecurityScheme()
                    .name(BEARER_SCHEME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            ));
    }

    @Bean
    public OpenApiCustomizer tenantHeaderCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap().forEach((method, operation) -> {
                if (isPublicPath(path)) {
                    operation.setSecurity(new ArrayList<>());
                } else {
                    ensureTenantHeader(operation);
                }

                if (isPaginatedListPath(path, method)) {
                    ensurePaginationParams(operation);
                }
            }));
        };
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/");
    }

    private boolean isPaginatedListPath(String path, PathItem.HttpMethod method) {
        return method == PathItem.HttpMethod.GET && PAGINATED_LIST_PATHS.contains(path);
    }

    private void ensureTenantHeader(io.swagger.v3.oas.models.Operation operation) {
        if (operation.getParameters() == null) {
            operation.setParameters(new ArrayList<>());
        }
        boolean alreadyPresent = operation.getParameters()
            .stream()
            .anyMatch(parameter -> TENANT_HEADER.equalsIgnoreCase(parameter.getName()));
        if (!alreadyPresent) {
            operation.addParametersItem(new Parameter()
                .in("header")
                .name(TENANT_HEADER)
                .required(true)
                .description("Tenant identifier for request isolation")
                .schema(new StringSchema())
                .example("1"));
        }
    }

    private void ensurePaginationParams(io.swagger.v3.oas.models.Operation operation) {
        if (operation.getParameters() == null) {
            operation.setParameters(new ArrayList<>());
        }
        addQueryParamIfAbsent(
            operation,
            "page",
            "Zero-based page index (default: 0)",
            new IntegerSchema()._default(0).minimum(java.math.BigDecimal.ZERO)
        );
        addQueryParamIfAbsent(
            operation,
            "size",
            "Page size (default: 20)",
            new IntegerSchema()._default(20).minimum(java.math.BigDecimal.ONE)
        );
        addQueryParamIfAbsent(
            operation,
            "sort",
            "Sort criteria in the format: property,(asc|desc). Repeat for multiple sorts.",
            new StringSchema().example("createdAt,desc")
        );
    }

    private void addQueryParamIfAbsent(
        io.swagger.v3.oas.models.Operation operation,
        String name,
        String description,
        io.swagger.v3.oas.models.media.Schema<?> schema
    ) {
        boolean exists = operation.getParameters().stream().anyMatch(parameter -> name.equalsIgnoreCase(parameter.getName()));
        if (!exists) {
            operation.addParametersItem(new Parameter()
                .in("query")
                .name(name)
                .required(false)
                .description(description)
                .schema(schema));
        }
    }
}
