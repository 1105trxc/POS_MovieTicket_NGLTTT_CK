package com.cinema.management.model.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    private String customerId;
    private String fullName;
    private String phone;
    private String email;
    private Integer rewardPoints;
    private BigDecimal totalSpent;
    private String memberTier;
}
