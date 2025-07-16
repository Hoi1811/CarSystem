package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Data
// Thêm callSuper = true vì bây giờ nó kế thừa BaseEntity
@EqualsAndHashCode(callSuper = true)
// Thêm SQLDelete để kích hoạt Soft Delete cho User
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?")
public class User extends BaseEntity implements UserDetails { // <-- Bước 1: Kế thừa BaseEntity

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;   // Hashed password

    @Column(name = "full_name")
    private String fullName;

    private String picture;

    @Column
    private String provider; // "google", "github", etc.

    @Column(name = "external_id")
    private String externalId; // ID từ OAuth2 provider

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @ManyToMany(fetch = FetchType.EAGER) // Nên dùng EAGER cho roles để lấy ra cùng lúc với User
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Nên lấy quyền từ Set<Role> thì sẽ đúng hơn
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Một tài khoản bị xóa mềm (deletedAt != null) cũng nên bị coi là khóa
        return this.getDeletedAt() == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }
}