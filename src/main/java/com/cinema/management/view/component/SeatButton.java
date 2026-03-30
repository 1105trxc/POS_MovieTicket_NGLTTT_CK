package com.cinema.management.view.component;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.SeatStatusDto.Status;

import javax.swing.*;
import java.awt.*;

public class SeatButton extends JButton {
    private SeatStatusDto seatStatus;
    private final boolean isSofa;

    // Bảng màu FlatLaf chuẩn rạp phim
    private static final Color COLOR_AVAILABLE = new Color(255, 255, 255); // Trắng
    private static final Color COLOR_SELECTED = new Color(14, 165, 233);  // Xanh dương
    private static final Color COLOR_LOCKED = new Color(245, 158, 11);  // Cam
    private static final Color COLOR_BOOKED = new Color(148, 163, 184); // Xám
    private static final Color BORDER_AVAILABLE = new Color(203, 213, 225);

    public SeatButton(SeatStatusDto seatStatus, boolean isSofa) {
        this.seatStatus = seatStatus;
        this.isSofa = isSofa;

        if (isSofa) {
            setText("❤ " + seatStatus.getSeatNumber()); // Thêm icon tim cho ghế đôi
            setPreferredSize(new Dimension(98, 45)); // Chiều rộng = 45(ô1) + 8(gap) + 45(ô2)
        } else {
            setText(String.valueOf(seatStatus.getSeatNumber()));
            setPreferredSize(new Dimension(45, 45)); // Hình vuông tiêu chuẩn
        }

        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        updateStatus(seatStatus);
    }

    public void updateStatus(SeatStatusDto newStatus) {
        this.seatStatus = newStatus;
        Status status = seatStatus.getStatus();

        switch (status) {
            case AVAILABLE:
                // Phân loại màu theo Tên loại ghế (Không phân biệt hoa thường)
                String typeName = seatStatus.getSeatTypeName().toLowerCase();

                if (typeName.contains("vip")) {
                    setBackground(new Color(254, 205, 211)); // Nền Hồng nhạt
                    setBorder(BorderFactory.createLineBorder(new Color(244, 63, 94), 2)); // Viền Hồng đậm
                } else if (typeName.contains("couple") || typeName.contains("sofa") || typeName.contains("sweetbox")) {
                    setBackground(new Color(233, 213, 255)); // Nền Tím nhạt
                    setBorder(BorderFactory.createLineBorder(new Color(168, 85, 247), 2)); // Viền Tím đậm
                } else {
                    setBackground(COLOR_AVAILABLE); // Nền Trắng (Standard)
                    setBorder(BorderFactory.createLineBorder(BORDER_AVAILABLE, 2));
                }

                setForeground(new Color(15, 23, 42)); // Chữ đen
                setEnabled(true);
                setToolTipText(String.format("Ghế %s - %s - %,.0f VNĐ",
                        seatStatus.getLabel(), seatStatus.getSeatTypeName(), seatStatus.getBasePrice()));
                break;

            case SELECTED:
                setBackground(COLOR_SELECTED);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(COLOR_SELECTED.darker(), 2));
                setEnabled(true);
                setToolTipText("Ghế bạn đang chọn");
                break;

            case LOCKED:
                setBackground(COLOR_LOCKED);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(COLOR_LOCKED.darker(), 2));
                setEnabled(false); // Khóa không cho bấm
                setToolTipText("Ghế " + seatStatus.getLabel() + " đang được nhân viên khác thao tác");
                break;

            case BOOKED:
                setBackground(COLOR_BOOKED);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(COLOR_BOOKED.darker(), 2));
                setEnabled(false); // Đã bán thì nghỉ bấm
                setToolTipText("Ghế " + seatStatus.getLabel() + " đã thanh toán");
                break;
        }
    }

    public String getSeatId() {
        return seatStatus.getSeatId();
    }

    public Status getStatus() {
        return seatStatus.getStatus();
    }

    public SeatStatusDto getSeatStatus() {
        return seatStatus;
    }
}