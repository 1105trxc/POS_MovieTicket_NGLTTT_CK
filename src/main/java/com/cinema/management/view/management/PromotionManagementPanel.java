package com.cinema.management.view.management;

import javax.swing.*;
import java.awt.*;

public class PromotionManagementPanel extends JPanel {

    public PromotionManagementPanel() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));
        JLabel lbl = new JLabel("Quan ly khuyen mai - dang phat trien");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(100, 116, 139));
        add(lbl);
    }
}
