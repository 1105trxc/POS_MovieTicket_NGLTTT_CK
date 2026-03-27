package com.cinema.management.util;

import com.cinema.management.model.entity.User;

public class UserSessionContext {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
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
}
