package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @Column(name = "InvoiceID", length = 50)
    private String invoiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerID")
    @ToString.Exclude
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PromotionID")
    @ToString.Exclude
    private Promotion promotion;

    @Column(name = "TotalAmount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "UsedPoints")
    @Builder.Default
    private Integer usedPoints = 0;

    @Column(name = "DiscountFromPoints", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountFromPoints = BigDecimal.ZERO;

    @Column(name = "EarnedPoints")
    @Builder.Default
    private Integer earnedPoints = 0;

    @Column(name = "DiscountFromTier", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountFromTier = BigDecimal.ZERO;

    @Column(name = "DiscountFromPromotion", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountFromPromotion = BigDecimal.ZERO;

    @Column(name = "FinalAmount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(name = "CreatedAt")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "Status", length = 20)
    @Builder.Default
    private String status = "COMPLETED"; // COMPLETED, CANCELED, REFUNDED

    @Column(name = "CancellationReason", length = 500)
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovedBy")
    @ToString.Exclude
    private User approvedBy;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<BookingSeat> bookingSeats;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Payment> payments;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PointHistory> pointHistories;
}
