package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "CustomerID", length = 50)
    private String customerId;

    @Column(name = "FullName", length = 255)
    private String fullName;

    @Column(name = "Phone", unique = true, length = 20)
    private String phone;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "RewardPoints")
    @Builder.Default
    private Integer rewardPoints = 0;

    @Column(name = "TotalSpent", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "MemberTier", length = 50)
    @Builder.Default
    private String memberTier = "Member";

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Invoice> invoices;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PointHistory> pointHistories;
}
