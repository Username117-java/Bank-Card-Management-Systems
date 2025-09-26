package com.example.bankcards.util;

public class CardMaskingUtil {
    public static String mask(String plainNumber) {
        if (plainNumber == null) return null;
        String digits = plainNumber.replaceAll("\\s+", "");
        if (digits.length() < 4) return "****";
        String last4 = digits.substring(digits.length() - 4);
        return "**** **** **** " + last4;
    }
}
