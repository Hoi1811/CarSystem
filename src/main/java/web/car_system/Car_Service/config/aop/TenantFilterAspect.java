package web.car_system.Car_Service.config.aop;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import web.car_system.Car_Service.domain.entity.User;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TenantFilterAspect {

    private final EntityManager entityManager;

    @Before("execution(* web.car_system.Car_Service.controller.*.*(..))")
    public void enableTenantFilter() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
                return; // Not authenticated or not our User entity
            }

            User currentUser = (User) authentication.getPrincipal();
            
            // Lấy Request hiện tại để đọc Header
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;
            
            HttpServletRequest request = attributes.getRequest();
            String tenantIdHeader = request.getHeader("X-Tenant-ID");
            
            Long activeTenantId = null;

            // Check if user is SYSTEM_ADMIN
            boolean isAdmin = currentUser.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ROLE_SYSTEM_ADMIN") || role.getName().equals("SYSTEM_ADMIN"));

            if (isAdmin) {
                if (tenantIdHeader != null && !tenantIdHeader.isEmpty()) {
                    // Admin is impersonating a specific showroom
                    activeTenantId = Long.parseLong(tenantIdHeader);
                    log.debug("System Admin viewing data for Tenant ID: {}", activeTenantId);
                } else {
                    // Admin viewing all data - do not enable filter
                    return;
                }
            } else {
                // Regular staff/manager - MUST be restricted to their own showroom
                if (currentUser.getShowroom() != null) {
                    activeTenantId = currentUser.getShowroom().getId();
                } else {
                    // Staff doesn't belong to any showroom - shouldn't happen, but let's block them from seeing others' data
                    log.warn("User {} has no assigned showroom!", currentUser.getUsername());
                    // Assign a dummy huge ID so they see nothing instead of everything
                    activeTenantId = -999L; 
                }
            }

            // Kích hoạt Hibernate Filter (Chặn đứng SQL Query toàn cục)
            if (activeTenantId != null) {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("tenantId", activeTenantId);
                log.debug("Tenant Filter enabled for Tenant ID: {}", activeTenantId);
            }
            
        } catch (Exception e) {
            log.error("Error setting up Tenant Filter", e);
        }
    }
}
