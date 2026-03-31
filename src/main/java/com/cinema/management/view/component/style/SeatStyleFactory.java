package com.cinema.management.view.component.style;

import javax.swing.JButton;
import java.util.ArrayList;
import java.util.List;

public class SeatStyleFactory {
    // Danh sách lưu trữ các chiến lược
    private static final List<ISeatStyleStrategy> strategies = new ArrayList<>();

    // Khối static: Đăng ký các chiến lược vào hệ thống
    static {
        strategies.add(new VipSeatStyle());
        strategies.add(new CoupleSeatStyle());
        // Standard sẽ được gọi thủ công dưới dạng Fallback
    }

    public static void applyAvailableStyle(String typeName, JButton button) {
        // Duyệt qua danh sách, ai báo "Match" thì giao cho người đó tô màu
        for (ISeatStyleStrategy strategy : strategies) {
            if (strategy.isMatch(typeName)) {
                strategy.applyStyle(button);
                return;
            }
        }
        // Fallback mặc định nếu không khớp ai
        new StandardSeatStyle().applyStyle(button);
    }
}