package com.cinema.management.service;

import com.cinema.management.model.entity.Promotion;
import com.cinema.management.model.entity.ShowTime;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Interface nghiệp vụ kiểm tra và áp dụng mã khuyến mãi (FR-ST-03).
 */
public interface IPromotionService {

    /**
     * Kiểm tra mã promo có hợp lệ với suất chiếu đang chọn không.
     * Điều kiện: chưa hết hạn, đúng phim (nếu có), đúng ngày trong tuần (nếu có).
     *
     * @param code         mã promo nhập vào
     * @param showTime     suất chiếu hiện tại
     * @return Promotion nếu hợp lệ
     * @throws IllegalArgumentException nếu mã không tồn tại hoặc không hợp lệ
     */
    Promotion validatePromoCode(String code, ShowTime showTime);

    /**
     * Tính số tiền được giảm dựa trên tổng tiền ghế và promotion.
     * Áp dụng maxDiscountAmount nếu có.
     */
    BigDecimal calculateDiscount(Promotion promotion, BigDecimal subTotal);

    /**
     * Kiểm tra promotion có IsExclusive không (BR-01).
     * Nếu exclusive → không được dùng điểm thưởng cùng lúc.
     */
    boolean isExclusive(Promotion promotion);
}

