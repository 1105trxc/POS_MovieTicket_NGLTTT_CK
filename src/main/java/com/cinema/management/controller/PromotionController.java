package com.cinema.management.controller;

import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.Promotion;
import com.cinema.management.repository.AuditLogRepository;
import com.cinema.management.repository.PromotionRepository;
import com.cinema.management.service.IPromotionService;
import com.cinema.management.service.impl.AuditLogServiceImpl;
import com.cinema.management.service.impl.PromotionServiceImpl;

import java.math.BigDecimal;
import java.util.List;

public class PromotionController {

    private final IPromotionService promotionService;

    public PromotionController() {
        // DI thủ công (manual wiring)
        this.promotionService = new PromotionServiceImpl(
                new PromotionRepository(),
                new AuditLogServiceImpl(new AuditLogRepository()));
    }

    // ===== CRUD cho Admin Panel =====

    public List<Promotion> getAllPromotions() {
        return promotionService.getAllPromotions();
    }

    public Promotion getPromotionById(String id) {
        return promotionService.getPromotionById(id);
    }

    public Promotion getPromotionByCode(String code) {
        return promotionService.getPromotionByCode(code);
    }

    public void addPromotion(Promotion promotion) {
        promotionService.createPromotion(promotion);
    }

    public void updatePromotion(Promotion promotion) {
        promotionService.updatePromotion(promotion);
    }

    public void deletePromotion(String id) {
        promotionService.deletePromotion(id);
    }

    // ===== Logic cho Checkout =====

    /**
     * Lấy danh sách KM đang hoạt động (dùng cho hiển thị hoặc chọn thủ công).
     */
    public List<Promotion> getActivePromotions() {
        return promotionService.getActivePromotions();
    }

    /**
     * Tìm KM tốt nhất tự động cho 1 phim và 1 số tiền.
     */
    public Promotion findBestPromotion(Movie movie, BigDecimal amount) {
        return promotionService.findBestApplicablePromotion(movie, amount);
    }

    /**
     * Tính giảm giá từ 1 KM cụ thể.
     */
    public BigDecimal calculateDiscount(Promotion promotion, BigDecimal amount) {
        return promotionService.calculatePromotionDiscount(promotion, amount);
    }
}
