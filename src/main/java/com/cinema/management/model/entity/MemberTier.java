package com.cinema.management.model.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Enum đại diện các hạng thành viên của khách hàng.
 * <ul>
 * <li>GOLD — Giảm 10%, yêu cầu tổng chi tiêu ≥ 5,000,000đ</li>
 * <li>SILVER — Giảm 5%, yêu cầu tổng chi tiêu ≥ 2,000,000đ</li>
 * <li>REGULAR — Không giảm, mặc định</li>
 * </ul>
 * Điểm tích lũy = 5% tổng hóa đơn (sau giảm giá).
 */
public enum MemberTier {

    GOLD("Vàng", new BigDecimal("10"), new BigDecimal("5000000")),
    SILVER("Bạc", new BigDecimal("5"), new BigDecimal("2000000")),
    REGULAR("Thường", BigDecimal.ZERO, BigDecimal.ZERO);

    private final String displayName;
    private final BigDecimal discountPercent;
    private final BigDecimal requiredSpent;

    /** Tỉ lệ tích điểm cố định: 5% giá trị hóa đơn. */
    public static final BigDecimal POINT_EARN_RATE = new BigDecimal("0.05");

    MemberTier(String displayName, BigDecimal discountPercent, BigDecimal requiredSpent) {
        this.displayName = displayName;
        this.discountPercent = discountPercent;
        this.requiredSpent = requiredSpent;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Phần trăm giảm giá (VD: 10 = 10%). */
    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    /** Tổng chi tiêu tối thiểu để đạt hạng này. */
    public BigDecimal getRequiredSpent() {
        return requiredSpent;
    }

    /**
     * Tính số tiền giảm giá cho một tổng tiền cho trước.
     * VD: GOLD → totalAmount * 10 / 100
     */
    public BigDecimal calculateDiscount(BigDecimal totalAmount) {
        if (totalAmount == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalAmount.multiply(discountPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /**
     * Xác định hạng thành viên dựa trên tổng chi tiêu.
     * Ưu tiên từ cao → thấp: GOLD → SILVER → REGULAR.
     */
    public static MemberTier determineTier(BigDecimal totalSpent) {
        if (totalSpent == null)
            return REGULAR;
        if (totalSpent.compareTo(GOLD.requiredSpent) >= 0)
            return GOLD;
        if (totalSpent.compareTo(SILVER.requiredSpent) >= 0)
            return SILVER;
        return REGULAR;
    }

    /**
     * Parse từ chuỗi lưu trong DB (không phân biệt hoa thường).
     * Hỗ trợ cả tên tiếng Việt ("Vàng", "Bạc") và tên enum ("GOLD", "SILVER").
     */
    public static MemberTier fromString(String value) {
        if (value == null || value.isBlank())
            return REGULAR;
        for (MemberTier tier : values()) {
            if (tier.name().equalsIgnoreCase(value.trim())
                    || tier.displayName.equalsIgnoreCase(value.trim())) {
                return tier;
            }
        }
        // Fallback: "Member" cũ trong DB → REGULAR
        return REGULAR;
    }

    /**
     * Tính điểm tích lũy từ tổng hóa đơn (sau giảm giá).
     * Điểm = finalAmount * 5% (làm tròn xuống thành int).
     */
    public static int calculateEarnedPoints(BigDecimal finalAmount) {
        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0)
            return 0;
        return finalAmount.multiply(POINT_EARN_RATE)
                .setScale(0, RoundingMode.FLOOR)
                .intValue();
    }
}
