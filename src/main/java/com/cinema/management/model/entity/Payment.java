package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @Column(name = "PaymentID", length = 50)
    private String paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InvoiceID", nullable = false)
    @ToString.Exclude
    private Invoice invoice;

    @Column(name = "Amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "PaymentMethod", length = 50)
    private String paymentMethod;

    @Column(name = "TransactionCode", length = 100)
    private String transactionCode;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "CreatedAt")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
