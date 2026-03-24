package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "OrderDetail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    @EmbeddedId
    private OrderDetailId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("invoiceId")
    @JoinColumn(name = "InvoiceID")
    @ToString.Exclude
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "ProductID")
    @ToString.Exclude
    private Product product;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    /**
     * Giá snapshot tại thời điểm đặt hàng.
     * Không phụ thuộc vào Product.CurrentPrice về sau.
     */
    @Column(name = "Price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
}
