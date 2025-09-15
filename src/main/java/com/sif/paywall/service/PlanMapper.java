package com.sif.paywall.service;

public class PlanMapper {
    public static String getProfileForAmount(double amount) {
        if (amount == 3000) return "monthly";   // ₦500 → 1-day
        if (amount == 6000) return "monthly"; // ₦1000 → 7-days
        if (amount == 15000) return "monthly";// ₦3000 → 30-days
        return "default"; // fallback
    }

    public static int getDurationDays(double amount) {
        if (amount == 500) return 30;
        if (amount == 1000) return 30;
        if (amount == 3000) return 30;
        return 1;
    }
}
