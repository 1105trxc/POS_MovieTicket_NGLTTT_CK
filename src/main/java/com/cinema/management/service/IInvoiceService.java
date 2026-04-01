package com.cinema.management.service;

import com.cinema.management.model.dto.InvoiceDto;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * Interface nghiệp vụ Thanh toán & Xuất vé (Module 3, FR-ST-04).
 *
 * Luồng:
 *   1. Nhân viên xác nhận danh sách ghế (từ SeatLock).
 *   2. Nhập mã promo và/hoặc điểm thưởng (tuỳ chọn).
 *   3. Chọn phương thức thanh toán.
 *   4. Gọi checkout() → hệ thống lưu Invoice + BookingSeat + OrderDetail
 *      + Payment + PointHistory, xoá SeatLock, cộng điểm.
 *   5. Trả về InvoiceDto để hiển thị hoá đơn + in vé.
 */
public interface IInvoiceService {

    /**
     * Thực hiện thanh toán toàn bộ đơn.
     *
     * @param showTimeId      suất chiếu
     * @param staffUserId     nhân viên thực hiện
     * @param customerId      khách hàng (null nếu không có thành viên)
     * @param selectedSeats   danh sách ghế đang SELECTED
     * @param fbItems         map productId → quantity (F&B), có thể rỗng
     * @param promoCode       mã khuyến mãi (null nếu không có)
     * @param usedPoints      số điểm muốn dùng (0 nếu không dùng)
     * @param paymentMethod   "CASH" | "CARD" | "TRANSFER"
     * @return InvoiceDto đầy đủ để hiển thị và in
     * @throws IllegalStateException    nếu ghế đã hết lock hoặc đã bị người khác mua
     * @throws IllegalArgumentException nếu mã promo không hợp lệ hoặc không đủ điểm
     */
    InvoiceDto checkout(String showTimeId,
                        String staffUserId,
                        String customerId,
                        List<SeatStatusDto> selectedSeats,
                        Map<String, Integer> fbItems,
                        String promoCode,
                        int usedPoints,
                        String paymentMethod);

    /**
     * Lay trang thai thanh toan theo paymentId.
     * @return PENDING | SUCCESS | FAILED
     */
    String getPaymentStatus(String paymentId);

    /**
     * Xac nhan thanh toan QR thanh cong (co the duoc goi boi webhook/callback).
     */
    boolean confirmQrPayment(String paymentId, String transactionCode);

    /**
     * Lay thong tin hoa don dang cho thanh toan QR theo paymentId.
     */
    InvoiceDto findPendingInvoiceByPaymentId(String paymentId, String staffUserId);
}

