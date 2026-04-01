package com.cinema.management.view.component.style;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;

public class StandardSeatStyle implements ISeatStyleStrategy {
    @Override
    public boolean isMatch(String seatTypeName) {
        return true; // Luôn trả về true vì đây là Fallback mặc định
    }

    @Override
    public void applyStyle(JButton button) {
        button.setBackground(Color.WHITE); // Nền Trắng
        button.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 2));
        button.setForeground(new Color(15, 23, 42));
    }
}