package com.cinema.management.util;

public class FormatUtil {

    /**
     * Chuyển đổi chuỗi tiền tệ (có dấu phẩy) thành số thực.
     * VD: "50,000" -> 50000.0
     * @param currencyString
     * @return 
     */
    public static double parseCurrency(String currencyString) {
        if (currencyString == null || currencyString.trim().isEmpty()) {
            return 0.0;
        }
        try {
            // Loại bỏ dấu phẩy
            String cleanString = currencyString.replace(",", "").trim();
            return Double.parseDouble(cleanString);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * Định dạng số thực thành chuỗi tiền tệ (có dấu phẩy).
     * VD: 50000 -> "50,000"
     */
    public static String formatCurrency(double amount) {
        return String.format("%,.0f", amount);
    }
}
