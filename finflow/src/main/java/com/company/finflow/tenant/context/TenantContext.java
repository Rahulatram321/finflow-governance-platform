package com.company.finflow.tenant.context;

public final class TenantContext {

    private static final ThreadLocal<Long> TENANT_HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_HOLDER.get();
    }

    public static Long getRequiredTenantId() {
        Long tenantId = TENANT_HOLDER.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is not set for current request");
        }
        return tenantId;
    }

    public static void clear() {
        TENANT_HOLDER.remove();
    }
}
