# FinFlow — Multi-Tenant Financial Governance Platform

## Overview

FinFlow is a multi-tenant financial workflow governance platform designed to control the complete expense lifecycle — from draft creation to final approval — with built-in budget enforcement, role-based controls, and full audit traceability.

It solves a critical operational problem:

> How do organizations move spending decisions quickly without compromising governance, compliance, or financial control?

FinFlow provides deterministic approval workflows, tenant isolation, and real-time budget validation to ensure spending decisions remain controlled, traceable, and policy-compliant.

---

## Core Capabilities

- Multi-tenant SaaS architecture
- JWT-based authentication with tenant-scoped validation
- Role-based access control (ADMIN, FINANCE_MANAGER, EMPLOYEE, AUDITOR)
- Deterministic multi-step workflow engine
- Budget validation before final approval
- Pessimistic database locking to prevent race conditions
- Structured audit logging for compliance
- Operational dashboards and analytics endpoints
- OpenAPI documentation and health monitoring endpoints

---

## System Architecture

FinFlow follows a layered architecture with strict separation of concerns:

User  
→ JWT Authentication  
→ Tenant Validation Filter  
→ Controller Layer  
→ Service Layer (Business Logic)  
→ Repository Layer  
→ PostgreSQL Database  

### Architecture Flow

1. User authenticates with email, password, and tenantId.
2. Backend validates credentials and issues JWT containing:
   - userId
   - tenantId
   - role
3. Frontend attaches:
   - Authorization: Bearer <token>
   - X-Tenant-Id header
4. TenantFilter validates tenant isolation before controller execution.
5. Business logic executes within service layer.
6. Budget validation and workflow integrity checks occur before final approval.
7. Critical actions generate structured audit logs.

---

## System Architecture Diagram

<img width="968" height="648" alt="image" src="https://github.com/user-attachments/assets/3b113629-b62a-42e0-a2b7-f4e054f134f0" />


---

## Workflow Lifecycle

Expense Status Flow:

DRAFT  
→ SUBMITTED  
→ MANAGER_APPROVAL  
→ (Optional ADMIN_APPROVAL)  
→ FINANCE_APPROVAL  
→ FINALIZED  

Governance guarantees:

- Self-approval is blocked
- Step sequence integrity is enforced
- Cannot approve out of order
- Budget is validated before final finance approval
- Budget spend is applied atomically
- Rejections auto-skip remaining workflow steps

---

## Budget Enforcement Logic

Before final approval:

- Active budget for expense date is validated
- Approval blocked if utilization exceeds 100%
- Warning triggered at >= 80%
- Spend amount applied using pessimistic locking
- Prevents concurrent race conditions

This ensures financial integrity under concurrent access.

---

## Audit and Compliance

Every critical action writes structured audit entries:

- Entity type
- Action performed
- Old values
- New values
- User
- Tenant
- Timestamp

Audit logs are:

- Tenant-scoped
- Paginated
- Filterable
- Exportable

Designed for compliance and traceability.

---

## Data Model

Core Entities:

- tenants
- users
- expenses
- budgets
- workflow_steps
- audit_logs

Governance Constraints:

- Non-negative monetary values
- Enum validation for roles and statuses
- Workflow step order uniqueness
- Tenant-scoped foreign key isolation

Database migrations enforce schema-level integrity.

---

## Security Model

- JWT authentication
- Tenant-bound session enforcement
- Role-based endpoint protection
- Method-level authorization controls
- Centralized global exception handling
- CORS configuration with tenant header support

---

## Technology Stack

### Backend
- Spring Boot
- PostgreSQL
- JWT Authentication
- Flyway Migrations
- Layered Architecture (Controller → Service → Repository)

### Frontend
- Next.js
- Role-aware protected routing
- Token-based session management


---

## Setup Instructions

### Backend


mvn clean install
mvn spring-boot:run


### Frontend


npm install
npm run dev


---

## Production Considerations

- Tenant isolation enforced at filter level
- Deterministic workflow sequencing
- Atomic budget updates
- Global error handling strategy
- Health endpoint for monitoring
- Environment-based configuration separation

---

## Positioning

FinFlow is not an expense tracker.

It is a governance control plane for organizational spend.

It enforces financial policy before money leaves the system.
It provides transparency across approval layers.
It maintains compliance-grade traceability.

Built with scalability, isolation, and operational control in mind.
