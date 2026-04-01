package com.cinema.management.view.component;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.SeatStatusDto.Status;
import com.cinema.management.view.component.style.SeatStyleFactory;

import javax.swing.*;
import java.awt.*;

public class SeatButton extends JButton {
    private SeatStatusDto seatStatus;

    private static final Color COLOR_SELECTED = new Color(14, 165, 233);
    private static final Color COLOR_LOCKED = new Color(245, 158, 11);
    private static final Color COLOR_BOOKED = new Color(148, 163, 184);

    public SeatButton(SeatStatusDto seatStatus, boolean isSofa) {
        this.seatStatus = seatStatus;

        if (isSofa) {
            setText("❤ " + seatStatus.getSeatNumber());
            setPreferredSize(new Dimension(98, 45));
        } else {
            setText(String.valueOf(seatStatus.getSeatNumber()));
            setPreferredSize(new Dimension(45, 45));
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
                SeatStyleFactory.applyAvailableStyle(seatStatus.getSeatTypeName(), this);
                setEnabled(true);
                setToolTipText(String.format("Ghe %s - %s - %,.0f VND",
                        seatStatus.getLabel(), seatStatus.getSeatTypeName(), seatStatus.getBasePrice()));
                break;
            case SELECTED:
                setBackground(COLOR_SELECTED);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(COLOR_SELECTED.darker(), 2));
                setEnabled(true);
                setToolTipText("Ghe ban dang chon");
                break;
            case LOCKED:
                setBackground(COLOR_LOCKED);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(COLOR_LOCKED.darker(), 2));
                setEnabled(false);
                setToolTipText("Ghe dang duoc nhan vien khac thao tac");
                break;
            case PROCESSING:
                setBackground(COLOR_LOCKED);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(COLOR_LOCKED.darker(), 2));
                boolean canResumePayment = seatStatus.getPendingPaymentId() != null
                        && !seatStatus.getPendingPaymentId().isBlank();
                setEnabled(canResumePayment);
                if (canResumePayment) {
                    setToolTipText("Ghe dang cho thanh toan QR. Bam de tiep tuc thanh toan.");
                } else {
                    setToolTipText("Ghe dang duoc nhan vien khac xu ly thanh toan QR.");
                }
                break;
            case BOOKED:
                setBackground(COLOR_BOOKED);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(COLOR_BOOKED.darker(), 2));
                setEnabled(false);
                setToolTipText("Ghe da thanh toan");
                break;
            default:
                setEnabled(false);
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
