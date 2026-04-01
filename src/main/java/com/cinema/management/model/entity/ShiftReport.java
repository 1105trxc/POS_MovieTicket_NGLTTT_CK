package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ShiftReport")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftReport {

    @Id
    @Column(name = "ShiftReportID", length = 50)
    private String shiftReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "ShiftStart", nullable = false)
    private LocalDateTime shiftStart;

    @Column(name = "ShiftEnd", nullable = false)
    private LocalDateTime shiftEnd;

    @Column(name = "OpeningCash", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal openingCash = BigDecimal.ZERO;

    @Column(name = "CashRevenue", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal cashRevenue = BigDecimal.ZERO;

    @Column(name = "TransferRevenue", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal transferRevenue = BigDecimal.ZERO;

    @Column(name = "CardRevenue", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal cardRevenue = BigDecimal.ZERO;

    @Column(name = "TotalRevenue", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "ExpectedCash", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal expectedCash = BigDecimal.ZERO;

    @Column(name = "ActualCash", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal actualCash = BigDecimal.ZERO;

    @Column(name = "Discrepancy", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal discrepancy = BigDecimal.ZERO;

    @Column(name = "RemittedCash", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal remittedCash = BigDecimal.ZERO;

    @Column(name = "CarryOverCash", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal carryOverCash = BigDecimal.ZERO;

    @Column(name = "CreatedAt", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "Status", length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, LOCKED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovedBy")
    @ToString.Exclude
    private User approvedBy;

    @Column(name = "ApprovedAt")
    private LocalDateTime approvedAt;

    @Column(name = "Notes", length = 500)
    private String notes;
}
