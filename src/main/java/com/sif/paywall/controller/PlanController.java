package com.sif.paywall.controller;


import com.sif.paywall.models.ConnectDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
    @RequestMapping("/api/plans")
    public class PlanController {

        @Autowired
        private ConnectDB connectDB;

        @PostMapping("/create")
        public Map<String, Object> createPlan(@RequestParam String name,
                                              @RequestParam int dataLimitMb,
                                              @RequestParam int durationDays,
                                              @RequestParam double price) {
            Map<String, Object> response = new HashMap<>();

            String sql = "INSERT INTO plans (name, data_limit_mb, duration_days, price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connectDB.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, dataLimitMb);
                pstmt.setInt(3, durationDays);
                pstmt.setDouble(4, price);
                pstmt.executeUpdate();
                response.put("success", true);
                response.put("message", "Plan created successfully.");
            } catch (SQLException e) {
                response.put("success", false);
                response.put("error", e.getMessage());
            }

            return response;
        }

        @GetMapping("/list")
        public List<Map<String, Object>> listPlans() {
            List<Map<String, Object>> plans = new ArrayList<>();
            String sql = "SELECT * FROM plans";
            try (Statement stmt = connectDB.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Map<String, Object> plan = new HashMap<>();
                    plan.put("id", rs.getInt("id"));
                    plan.put("name", rs.getString("name"));
                    plan.put("dataLimitMb", rs.getInt("data_limit_mb"));
                    plan.put("durationDays", rs.getInt("duration_days"));
                    plan.put("price", rs.getDouble("price"));
                    plans.add(plan);
                }
            } catch (SQLException e) {
                System.err.println("Error fetching plans: " + e.getMessage());
            }
            return plans;
        }
    }



