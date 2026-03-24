package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @Column(name = "RoleID", length = 50)
    private String roleId;

    @Column(name = "RoleName", nullable = false, length = 100)
    private String roleName;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<User> users;
}
