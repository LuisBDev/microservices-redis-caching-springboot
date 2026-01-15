package com.mspoc.users_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Luis Balarezo
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_active", columnList = "active"),
        @Index(name = "idx_users_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "preferences") // Evita lazy loading en toString
@EqualsAndHashCode(of = "id")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relación 1:1 con UserPreferences
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserPreferences preferences;

    /**
     * Helper method para establecer la relación bidireccional con UserPreferences.
     * Mantiene la consistencia en ambos lados de la relación.
     */
    public void setPreferences(UserPreferences preferences) {
        if (preferences == null) {
            if (this.preferences != null) {
                this.preferences.setUser(null);
            }
        } else {
            preferences.setUser(this);
        }
        this.preferences = preferences;
    }

    /**
     * Retorna el nombre completo del usuario.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Verifica si el usuario tiene preferencias configuradas.
     */
    public boolean hasPreferences() {
        return preferences != null;
    }
}
