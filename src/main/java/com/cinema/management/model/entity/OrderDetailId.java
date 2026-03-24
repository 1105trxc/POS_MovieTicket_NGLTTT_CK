package com.cinema.management.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderDetailId implements Serializable {

    @Column(name = "InvoiceID", length = 50)
    private String invoiceId;

    @Column(name = "ProductID", length = 50)
    private String productId;
}
