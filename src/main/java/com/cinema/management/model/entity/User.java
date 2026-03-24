package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "UserID", length = 50)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoleID", nullable = false)
    @ToString.Exclude
    private Role role;

    @Column(name = "Username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "FullName", length = 255)
    private String fullName;

    @Column(name = "IsActive", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "changedBy", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<AuditLog> auditLogs;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Invoice> invoices;

    @OneToMany(mappedBy = "lockedBy", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<SeatLock> seatLocks;
}
