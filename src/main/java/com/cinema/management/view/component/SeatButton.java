package com.cinema.management.view.component;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.SeatStatusDto.Status;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Custom JButton đại diện cho 1 ghế trên sơ đồ ghế (SeatMapPanel).
 *
 * Màu sắc theo trạng thái (FR-ST-01):
 *   AVAILABLE → xanh lá nhạt
 *   SELECTED  → xanh dương đậm (ghế mình đang chọn)
 *   LOCKED    → vàng cam (người khác đang giữ)
 *   BOOKED    → đỏ xám (đã bán)
 */
public class SeatButton extends JButton {

    // ── Màu theo trạng thái ──────────────────────────────────────────────────
    private static final Color COLOR_AVAILABLE = new Color(39, 174, 96);
    private static final Color COLOR_SELECTED  = new Color(41, 128, 185);
    private static final Color COLOR_LOCKED    = new Color(230, 126, 34);
    private static final Color COLOR_BOOKED    = new Color(149, 165, 166);
    private static final Color COLOR_TEXT_DARK = new Color(30, 30, 30);
    private static final Color COLOR_TEXT_LIGHT= Color.WHITE;

    private SeatStatusDto seatStatus;

    public SeatButton(SeatStatusDto seatStatus) {
        super(seatStatus.getLabel());
        this.seatStatus = seatStatus;
        applyStyle();
    }

    /** Cập nhật trạng thái và làm mới màu sắc. */
    public void updateStatus(SeatStatusDto newStatus) {
        this.seatStatus = newStatus;
        setText(newStatus.getLabel());
        applyStyle();
        repaint();
    }

    public SeatStatusDto getSeatStatus() {
        return seatStatus;
    }

    public String getSeatId() {
        return seatStatus.getSeatId();
    }

    public BigDecimal getBasePrice() {
        return seatStatus.getBasePrice();
    }

    public Status getStatus() {
        return seatStatus.getStatus();
    }

    // ── Style ─────────────────────────────────────────────────────────────────

    private void applyStyle() {
        Color bg = switch (seatStatus.getStatus()) {
            case AVAILABLE -> COLOR_AVAILABLE;
            case SELECTED  -> COLOR_SELECTED;
            case LOCKED    -> COLOR_LOCKED;
            case BOOKED    -> COLOR_BOOKED;
        };
        Color fg = (seatStatus.getStatus() == Status.AVAILABLE) ? COLOR_TEXT_DARK : COLOR_TEXT_LIGHT;

        setBackground(bg);
        setForeground(fg);
        setFont(new Font("Arial", Font.BOLD, 11));
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);
        setPreferredSize(new Dimension(46, 36));
        setMargin(new Insets(2, 2, 2, 2));

        // Không cho click nếu đã bán hoặc bị người khác khóa
        setEnabled(seatStatus.getStatus() == Status.AVAILABLE
                || seatStatus.getStatus() == Status.SELECTED);

        // Tooltip hiển thị loại ghế và giá
        String price = String.format("%,.0f", seatStatus.getBasePrice());
        String statusLabel = switch (seatStatus.getStatus()) {
            case AVAILABLE -> "Trống";
            case SELECTED  -> "Đang chọn";
            case LOCKED    -> "Đang bị giữ";
            case BOOKED    -> "Đã bán";
        };
        setToolTipText(String.format("<html>%s<br>Loại: %s<br>Giá: %s VNĐ<br>Trạng thái: %s</html>",
                seatStatus.getLabel(), seatStatus.getSeatTypeName(), price, statusLabel));
        setCursor(isEnabled()
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getDefaultCursor());
    }
}