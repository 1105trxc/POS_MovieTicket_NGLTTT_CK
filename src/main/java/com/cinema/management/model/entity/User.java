package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    @JoinColumn(name = "RoleID", nullable = true)
    @ToString.Exclude
    private Role role;

    @Column(name = "Username", nullable = true, unique = true, length = 100)
    private String username;

    @Column(name = "Password", nullable = true, length = 255)
    private String password;

    @Column(name = "FullName", length = 255)
    private String fullName;

    @Column(name = "IsActive", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "BirthDate")
    private LocalDate birthDate;

    @Column(name = "CCCD", length = 20, unique = true)
    private String cccd;

    @Column(name = "Gender", length = 10)
    private String gender;

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
