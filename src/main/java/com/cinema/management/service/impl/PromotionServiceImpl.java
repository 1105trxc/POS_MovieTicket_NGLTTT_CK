package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Promotion;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.repository.PromotionRepository;
import com.cinema.management.service.IPromotionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Triển khai Promo Engine (FR-ST-03, FR-AD-05).
 */
public class PromotionServiceImpl implements IPromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionServiceImpl() {
        this.promotionRepository = new PromotionRepository();
    }

    public PromotionServiceImpl(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    public Promotion validatePromoCode(String code, ShowTime showTime) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống.");
        }

        Promotion promo = promotionRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi '" + code + "' không tồn tại."));

        // Kiểm tra hạn sử dụng
        if (promo.getExpiryDate() != null && promo.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Mã khuyến mãi '" + code + "' đã hết hạn.");
        }

        // Kiểm tra điều kiện phim (FR-AD-05)
        if (promo.getApplyToMovie() != null && showTime.getMovie() != null) {
            if (!promo.getApplyToMovie().getMovieId().equals(showTime.getMovie().getMovieId())) {
                throw new IllegalArgumentException(
                        "Mã '" + code + "' chỉ áp dụng cho phim: " + promo.getApplyToMovie().getTitle());
            }
        }

        // Kiểm tra điều kiện ngày trong tuần (FR-AD-05)
        if (promo.getValidDays() != null && !promo.getValidDays().isBlank()) {
            DayOfWeek today = showTime.getStartTime().getDayOfWeek();
            List<String> validDays = Arrays.asList(promo.getValidDays().toUpperCase().split(","));
            if (!validDays.contains(today.name())) {
                throw new IllegalArgumentException(
                        "Mã '" + code + "' không áp dụng vào " + today + ".");
            }
        }

        return promo;
    }

    @Override
    public BigDecimal calculateDiscount(Promotion promotion, BigDecimal subTotal) {
        if (promotion == null || promotion.getDiscountPercent() == null) return BigDecimal.ZERO;

        BigDecimal discount = subTotal
                .multiply(promotion.getDiscountPercent())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);

        // Áp dụng giới hạn tối đa (nếu có)
        if (promotion.getMaxDiscountAmount() != null
                && discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
            discount = promotion.getMaxDiscountAmount();
        }
        return discount;
    }

    @Override
    public boolean isExclusive(Promotion promotion) {
        return promotion != null && Boolean.TRUE.equals(promotion.getIsExclusive());
    }
}

