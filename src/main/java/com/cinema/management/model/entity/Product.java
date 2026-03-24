package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @Column(name = "ProductID", length = 50)
    private String productId;

    @Column(name = "ProductName", nullable = false, length = 255)
    private String productName;

    @Column(name = "CurrentPrice", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<OrderDetail> orderDetails;
}
