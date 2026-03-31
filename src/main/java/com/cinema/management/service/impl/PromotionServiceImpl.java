package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.Promotion;
import com.cinema.management.repository.PromotionRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.IPromotionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class PromotionServiceImpl implements IPromotionService {

    private final PromotionRepository promotionRepo;
    private final IAuditLogService auditLogService;

    public PromotionServiceImpl(PromotionRepository promotionRepo, IAuditLogService auditLogService) {
        this.promotionRepo = promotionRepo;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<Promotion> getAllPromotions() {
        return promotionRepo.findAll();
    }

    @Override
    public Promotion getPromotionById(String id) {
        return promotionRepo.findById(id);
    }

    @Override
    public Promotion getPromotionByCode(String code) {
        return promotionRepo.findByCode(code);
    }

    @Override
    public List<Promotion> getActivePromotions() {
        return promotionRepo.findActivePromotions(LocalDate.now());
    }

    @Override
    public void createPromotion(Promotion promotion) {
        promotionRepo.save(promotion);
        auditLogService.logAction("CREATE", "Promotion",
                "ID: " + promotion.getPromotionId(), "None", promotion.getCode());
    }

    @Override
    public void updatePromotion(Promotion promotion) {
        promotionRepo.update(promotion);
        auditLogService.logAction("UPDATE", "Promotion",
                "Cập nhật khuyến mãi", promotion.getCode(),
                promotion.getDiscountPercent() + "%");
    }

    @Override
    public void deletePromotion(String id) {
        promotionRepo.delete(id);
        auditLogService.logAction("DELETE", "Promotion", "Xóa khuyến mãi", id, "None");
    }

    /**
     * Tính giảm giá = amount * discountPercent / 100,
     * nhưng không vượt quá maxDiscountAmount (nếu có).
     */
    @Override
    public BigDecimal calculatePromotionDiscount(Promotion promotion, BigDecimal amount) {
        if (promotion == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = amount.multiply(promotion.getDiscountPercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // Cap theo maxDiscountAmount nếu có
        if (promotion.getMaxDiscountAmount() != null
                && promotion.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0
                && discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
            discount = promotion.getMaxDiscountAmount();
        }

        return discount;
    }

    /**
     * Tìm KM tốt nhất đang hoạt động cho 1 phim.
     * Logic: Lọc KM active cho phim → tính giảm giá cho từng KM → chọn KM cho giảm nhiều nhất.
     * Cũng kiểm tra validDays (ngày trong tuần hợp lệ).
     */
    @Override
    public Promotion findBestApplicablePromotion(Movie movie, BigDecimal amount) {
        LocalDate today = LocalDate.now();
        String todayDay = today.getDayOfWeek().name(); // VD: "MONDAY"

        List<Promotion> activePromotions;
        if (movie != null && movie.getMovieId() != null) {
            activePromotions = promotionRepo.findActiveForMovie(movie.getMovieId(), today);
        } else {
            activePromotions = promotionRepo.findActivePromotions(today);
        }

        return activePromotions.stream()
                .filter(p -> isValidForDay(p, todayDay))
                .max(Comparator.comparing(p -> calculatePromotionDiscount(p, amount)))
                .orElse(null);
    }

    /**
     * Kiểm tra KM có hợp lệ cho ngày hôm nay không.
     * Nếu validDays == null hoặc rỗng → áp dụng mọi ngày.
     */
    private boolean isValidForDay(Promotion promotion, String todayDay) {
        String validDays = promotion.getValidDays();
        if (validDays == null || validDays.isBlank()) {
            return true; // Áp dụng mọi ngày
        }
        // VD: "MONDAY,TUESDAY,SATURDAY"
        String[] days = validDays.toUpperCase().split(",");
        for (String day : days) {
            if (day.trim().equals(todayDay)) {
                return true;
            }
        }
        return false;
    }
}
