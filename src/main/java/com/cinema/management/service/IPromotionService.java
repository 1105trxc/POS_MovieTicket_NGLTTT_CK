package com.cinema.management.service;

import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.Promotion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IPromotionService {

    List<Promotion> getAllPromotions();

    Promotion getPromotionById(String id);

    Promotion getPromotionByCode(String code);

    /**
     * Lấy danh sách KM đang trong thời gian hiệu lực.
     */
    List<Promotion> getActivePromotions();

    void createPromotion(Promotion promotion);

    void updatePromotion(Promotion promotion);

    void deletePromotion(String id);

    /**
     * Tính số tiền giảm từ 1 khuyến mãi, có cap theo maxDiscountAmount.
     */
    BigDecimal calculatePromotionDiscount(Promotion promotion, BigDecimal amount);

    /**
     * Tìm khuyến mãi tốt nhất (giảm nhiều nhất) đang hoạt động,
     * phù hợp với phim đang đặt vé và ngày hiện tại.
     * Trả về null nếu không có KM nào.
     */
    Promotion findBestApplicablePromotion(Movie movie, BigDecimal amount);
}
