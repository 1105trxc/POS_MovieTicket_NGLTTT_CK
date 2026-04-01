package com.cinema.management.view.component.style;

import javax.swing.JButton;

public interface ISeatStyleStrategy {
    // Phương thức kiểm tra xem chiến lược này có áp dụng cho loại ghế này không
    boolean isMatch(String seatTypeName);

    // Phương thức thực thi việc tô màu/định dạng
    void applyStyle(JButton button);
}