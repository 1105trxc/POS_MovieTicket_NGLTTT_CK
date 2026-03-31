package com.cinema.management.view.component.style;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;

public class CoupleSeatStyle implements ISeatStyleStrategy {
    @Override
    public boolean isMatch(String seatTypeName) {
        String type = seatTypeName.toLowerCase();
        return type.contains("couple") || type.contains("sofa") || type.contains("sweetbox");
    }

    @Override
    public void applyStyle(JButton button) {
        button.setBackground(new Color(233, 213, 255)); // Nền Tím
        button.setBorder(BorderFactory.createLineBorder(new Color(168, 85, 247), 2));
        button.setForeground(new Color(15, 23, 42));
    }
}