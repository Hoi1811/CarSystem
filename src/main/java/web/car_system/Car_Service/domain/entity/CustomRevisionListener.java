package web.car_system.Car_Service.domain.entity;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevision = (CustomRevisionEntity) revisionEntity;

        // Lấy thông tin người dùng từ Spring Security Context
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            customRevision.setUsername(((UserDetails) principal).getUsername());
        } else if (principal != null) {
            customRevision.setUsername(principal.toString());
        } else {
            customRevision.setUsername("anonymous"); // Hoặc một giá trị mặc định nào đó
        }
    }
}