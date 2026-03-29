package com.cinema.management.view.component;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.SeatStatusDto.Status;

import javax.swing.*;
import java.awt.*;

public class SeatButton extends JButton {
    private SeatStatusDto seatStatus;

    // Bảng màu FlatLaf chuẩn rạp phim
    private static final Color COLOR_AVAILABLE = new Color(255, 255, 255); // Trắng
    private static final Color COLOR_SELECTED = new Color(14, 165, 233);  // Xanh dương
    private static final Color COLOR_LOCKED = new Color(245, 158, 11);  // Cam (Người khác đang giữ)
    private static final Color COLOR_BOOKED = new Color(148, 163, 184); // Xám (Đã bán)

    private static final Color BORDER_AVAILABLE = new Color(203, 213, 225);

    public SeatButton(SeatStatusDto seatStatus) {
        this.seatStatus = seatStatus;

        // Chỉ in số ghế (VD: "1", "2") vì chữ cái (VD: "A") sẽ nằm ở đầu hàng
        setText(String.valueOf(seatStatus.getSeatNumber()));
        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(45, 45)); // Đảm bảo hình vuông

        updateStatus(seatStatus);
    }

    public void updateStatus(SeatStatusDto newStatus) {
        this.seatStatus = newStatus;
        Status status = seatStatus.getStatus();

        switch (status) {
            case AVAILABLE:
                setBackground(COLOR_AVAILABLE);
                setForeground(new Color(15, 23, 42));
                setBorder(BorderFactory.createLineBorder(BORDER_AVAILABLE, 2));
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