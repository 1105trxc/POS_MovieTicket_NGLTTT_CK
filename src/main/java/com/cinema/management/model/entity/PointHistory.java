package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PointHistory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory {

    @Id
    @Column(name = "HistoryID", length = 50)
    private String historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerID", nullable = false)
    @ToString.Exclude
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InvoiceID")
    @ToString.Exclude
    private Invoice invoice;

    @Column(name = "PointAmount", nullable = false)
    private Integer pointAmount;

    /**
     * VD: "EARN" | "REDEEM"
     */
    @Column(name = "TransactionType", length = 50)
    private String transactionType;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "CreatedAt")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
