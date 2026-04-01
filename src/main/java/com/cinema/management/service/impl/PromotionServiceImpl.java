package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Promotion;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.repository.PromotionRepository;
import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.Promotion;
import com.cinema.management.repository.PromotionRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.IPromotionService;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PromotionServiceImpl implements IPromotionService {

    private final PromotionRepository promotionRepo;
    private final IAuditLogService auditLogService;

    public PromotionServiceImpl() {
        this.promotionRepo = new PromotionRepository();
        this.auditLogService = null; // Có thể inject sau nếu cần
    }

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
        return promotionRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Promotion ID '" + id + "' không tồn tại."));
    }

    @Override
    public Promotion getPromotionByCode(String code) {
        return promotionRepo.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Promotion code '" + code + "' không tồn tại."));
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

    @Override
    public Promotion validatePromoCode(String code, ShowTime showTime) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống.");
        }

        Promotion promo = promotionRepo.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi '" + code + "' không tồn tại."));

        // Kiểm tra hạn sử dụng
        if (promo.getExpiryDate() != null && promo.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Mã khuyến mãi '" + code + "' đã hết hạn.");
        }

        // Kiểm tra điều kiện phim (FR-AD-05)
        if (promo.getApplyToMovie() != null) {
            if (showTime == null || showTime.getMovie() == null) {
                throw new IllegalArgumentException(
                        "Mã '" + code + "' chỉ áp dụng khi mua vé phim: " + promo.getApplyToMovie().getTitle());
            }
            if (!promo.getApplyToMovie().getMovieId().equals(showTime.getMovie().getMovieId())) {
                throw new IllegalArgumentException(
                        "Mã '" + code + "' chỉ áp dụng cho phim: " + promo.getApplyToMovie().getTitle());
            }
        }

        // Kiểm tra điều kiện ngày trong tuần (FR-AD-05)
        if (promo.getValidDays() != null && !promo.getValidDays().isBlank()) {
            java.time.DayOfWeek today = showTime != null && showTime.getStartTime() != null 
                    ? showTime.getStartTime().getDayOfWeek() 
                    : LocalDate.now().getDayOfWeek();
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

