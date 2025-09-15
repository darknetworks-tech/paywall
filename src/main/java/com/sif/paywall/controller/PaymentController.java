package com.sif.paywall.controller;
import me.legrange.mikrotik.ApiConnection;


import com.sif.paywall.service.PaystackService;
import com.sif.paywall.models.ConnectDB;
import com.sif.paywall.service.PlanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaystackService paystackService;
    private final ConnectDB connectDB;

    @Autowired
    public PaymentController(PaystackService paystackService, ConnectDB connectDB) {
        this.paystackService = paystackService;
        this.connectDB = connectDB;
    }

    // ✅ Initialize payment
    @PostMapping("/init")
    public Map<String, Object> initPayment(@RequestParam String email,
                                           @RequestParam int planId) {
        Map<String, Object> response = new HashMap<>();
        String planName = "";
        double price = 0;
        int dataLimit = 0;
        int durationDays = 0;

        try (Connection conn = connectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT name, price, data_limit_mb, duration_days FROM plans WHERE id = ?")) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                planName = rs.getString("name");
                price = rs.getDouble("price");
                dataLimit = rs.getInt("data_limit_mb");
                durationDays = rs.getInt("duration_days");
            } else {
                response.put("success", false);
                response.put("message", "Plan not found for ID: " + planId);
                return response;
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("planId", planId);
        metadata.put("dataLimit", dataLimit);
        metadata.put("durationDays", durationDays);

        Map<String, Object> paystackResponse = paystackService.initializePayment(email, price, planName, metadata);
        response.put("success", true);
        response.put("paystack", paystackResponse);
        return response;
    }

    // ✅ Handle webhook from Paystack
    @PostMapping("/webhook")
    public Map<String, Object> handleWebhook(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String event = (String) payload.get("event");

            if ("charge.success".equals(event)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                String reference = (String) data.get("reference");
                String status = (String) data.get("status");
                String email = (String) ((Map<String, Object>) data.get("customer")).get("email");
                double amount = ((Number) data.get("amount")).doubleValue() / 100.0;

                Map<String, Object> metadata = (Map<String, Object>) data.get("metadata");
                int planId = (int) metadata.get("planId");
                int dataLimit = (int) metadata.get("dataLimit");
                int durationDays = (int) metadata.get("durationDays");

                int userId = -1;
                try (Connection conn = connectDB.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                    pstmt.setString(1, email);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    }
                }

                if (userId == -1) {
                    response.put("success", false);
                    response.put("message", "User not found for email: " + email);
                    return response;
                }

                LocalDate startDate = LocalDate.now();
                LocalDate endDate = startDate.plusDays(durationDays);

                // ✅ Insert payment record
                String paymentSql = "INSERT INTO payments (user_id, amount, plan_id, status, transaction_id, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (Connection conn = connectDB.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(paymentSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.setDouble(2, amount);
                    pstmt.setInt(3, planId);
                    pstmt.setString(4, status);
                    pstmt.setString(5, reference);
                    pstmt.setString(6, startDate.toString());
                    pstmt.setString(7, endDate.toString());
                    pstmt.executeUpdate();
                }

                // ✅ Create subscription
                String subSql = "INSERT INTO subscriptions (user_id, plan_id, start_date, end_date, status, data_limit_mb) VALUES (?, ?, ?, ?, ?, ?)";
                try (Connection conn = connectDB.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(subSql)) {
                    // inside handleWebhook after successful payment

                    String profile = PlanMapper.getProfileForAmount(amount);
                    int durationDays = PlanMapper.getDurationDays(amount);

                    // Activate subscription in DB
                    connectDB.activateUserSubscription(userId, durationDays);

                    // Create MikroTik user with correct profile
                    mikroTikService.createHotspotUser(username, password, profile);
                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, planId);
                    pstmt.setString(3, startDate.toString());
                    pstmt.setString(4, endDate.toString());
                    pstmt.setString(5, "active");
                    pstmt.setInt(6, dataLimit);
                    pstmt.executeUpdate();
                }

                response.put("success", true);
                response.put("message", "Payment and subscription created successfully.");
            } else {
                response.put("success", false);
                response.put("message", "Ignored event: " + event);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }


}

