package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Promotion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @Column(name = "PromotionID", length = 50)
    private String promotionId;

    @Column(name = "Code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "DiscountPercent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "MaxDiscountAmount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "ExpiryDate")
    private LocalDate expiryDate;

    /**
     * Điều kiện áp dụng theo phim (FR-AD-05).
     * NULL = áp dụng cho mọi phim.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApplyToMovie")
    @ToString.Exclude
    private Movie applyToMovie;

    /**
     * Điều kiện áp dụng theo thứ trong tuần (FR-AD-05).
     * VD: "MONDAY,TUESDAY,SATURDAY" — lưu dạng chuỗi phân cách bởi dấu phẩy.
     */
    @Column(name = "ValidDays", length = 100)
    private String validDays;

    /**
     * Không được cộng dồn với điểm thưởng (BR-01).
     */
    @Column(name = "IsExclusive", nullable = false)
    @Builder.Default
    private Boolean isExclusive = false;

    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Invoice> invoices;
}
