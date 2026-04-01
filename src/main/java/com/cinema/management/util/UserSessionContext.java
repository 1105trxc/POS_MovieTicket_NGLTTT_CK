package com.cinema.management.util;

import com.cinema.management.model.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserSessionContext {
    private static final BigDecimal STANDARD_OPENING_CASH = new BigDecimal("5000000");
    private static User currentUser;
    private static LocalDateTime shiftStartedAt;
    private static BigDecimal openingCash = BigDecimal.ZERO;

    public static void setCurrentUser(User user) {
        currentUser = user;
        shiftStartedAt = LocalDateTime.now();
        openingCash = STANDARD_OPENING_CASH;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
        shiftStartedAt = null;
        openingCash = BigDecimal.ZERO;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasRole(String roleName) {
        return currentUser != null && currentUser.getRole() != null
                && roleName.equalsIgnoreCase(currentUser.getRole().getRoleName());
    }

    /**
     * Kiểm tra Admin theo RoleID (R01) để tránh lỗi khi RoleName lưu tiếng Việt trong DB.
     * Cũng fallback check theo RoleName "ADMIN" hoặc "Quản trị viên".
     */
    public static boolean isAdmin() {
        if (currentUser == null || currentUser.getRole() == null) return false;
        String roleId   = currentUser.getRole().getRoleId();
        String roleName = currentUser.getRole().getRoleName();
        return "R01".equalsIgnoreCase(roleId)
                || "ADMIN".equalsIgnoreCase(roleName)
                || "Qu\u1ea3n tr\u1ecb vi\u00ean".equalsIgnoreCase(roleName);
    }

    public static LocalDateTime getShiftStartedAt() {
        return shiftStartedAt;
    }

    public static BigDecimal getOpeningCash() {
        return openingCash;
    }

    public static BigDecimal getStandardOpeningCash() {
        return STANDARD_OPENING_CASH;
    }

    public static void setOpeningCash(BigDecimal cash) {
        openingCash = cash != null ? cash : BigDecimal.ZERO;
    }
}
