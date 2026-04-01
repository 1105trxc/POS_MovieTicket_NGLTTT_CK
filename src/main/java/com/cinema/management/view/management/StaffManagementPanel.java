package com.cinema.management.view.management;

import javax.swing.*;
import java.awt.*;

public class StaffManagementPanel extends JPanel {

    public StaffManagementPanel() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));
        JLabel lbl = new JLabel("Quan ly nhan su - dang phat trien");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(100, 116, 139));
        add(lbl);
    }
}
