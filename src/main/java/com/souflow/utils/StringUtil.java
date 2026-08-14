package com.souflow.utils;

public class StringUtil {
    
    /**
     * Lọc bỏ các ký tự có thể gây SQL LIKE Injection trong SQL Server.
     * Ký tự %, _, [, và ] là các wildcard mặc định của SQL Server.
     */
    public static String sanitizeSqlLikeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return keyword.trim().replaceAll("[%_\\[\\]]", "");
    }
}
