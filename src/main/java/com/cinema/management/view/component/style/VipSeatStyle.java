package com.cinema.management.view.component.style;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;

public class VipSeatStyle implements ISeatStyleStrategy {
    @Override
    public boolean isMatch(String seatTypeName) {
        return seatTypeName.toLowerCase().contains("vip");
    }

    @Override
    public void applyStyle(JButton button) {
        button.setBackground(new Color(254, 205, 211)); // Nền Hồng
        button.setBorder(BorderFactory.createLineBorder(new Color(244, 63, 94), 2));
        button.setForeground(new Color(15, 23, 42));
    }
}