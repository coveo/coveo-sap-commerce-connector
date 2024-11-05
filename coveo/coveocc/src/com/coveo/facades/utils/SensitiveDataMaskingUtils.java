package com.coveo.facades.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveDataMaskingUtils {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^([a-zA-Z0-9_\\-.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}))$");
    private static final String MASK = "*****";

    public static boolean isEmail(String username) {
        Matcher matcher = EMAIL_PATTERN.matcher(username);
        return matcher.matches();
    }

    public static String maskEmail(String email) {
        if (isEmail(email)) {
            String[] parts = email.split("@");
            String[] domainParts = parts[1].split("\\.");

            StringBuilder maskedDomain = new StringBuilder(maskDomain(domainParts[0]));
            for (int i = 1; i < domainParts.length; i++) {
                maskedDomain.append(".").append(domainParts[i]);
            }

            return maskLocal(parts[0]) + "@" +  maskedDomain;
        }
        return email;
    }

    private static String maskLocal(String term) {
        return term.charAt(0) + MASK;
    }

    private static String maskDomain(String term) {
        return term.length() > 1? term.charAt(0) + MASK + term.charAt(term.length() - 1) : MASK;
    }
}
