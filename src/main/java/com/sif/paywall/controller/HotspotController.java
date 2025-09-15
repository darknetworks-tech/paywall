package com.sif.paywall.controller;

import com.sif.paywall.models.ConnectDB;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/hotspot")
public class HotspotController {

    private final ConnectDB connectDB;

    public HotspotController() {
        this.connectDB = new ConnectDB();
    }

    @PostMapping("/login")
    public Map<String, Object> hotspotLogin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String mac,
            @RequestParam(required = false) String ip
    ) {
        Map<String, Object> response = new HashMap<>();

        try (var conn = connectDB.getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    int userId = rs.getInt("id");

                    // ✅ Track the connection
                    if (mac != null && ip != null) {
                        connectDB.trackConnection(userId, mac, ip);
                    }

                    response.put("success", true);
                    response.put("message", "Login successful.");
                    response.put("userId", userId);
                } else {
                    response.put("success", false);
                    response.put("message", "Invalid password.");
                }
            } else {
                response.put("success", false);
                response.put("message", "User not found.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }
    @GetMapping("/connections")
    public Map<String, Object> getConnections() {
        Map<String, Object> response = new HashMap<>();
        var connections = connectDB.getAllConnections();

        response.put("success", true);
        response.put("count", connections.size());
        response.put("connections", connections);

        return response;
    }
    @DeleteMapping("/disconnect")
    public Map<String, Object> disconnect(@RequestParam String mac) {
        Map<String, Object> response = new HashMap<>();

        boolean success = connectDB.disconnectUser(mac);

        if (success) {
            response.put("success", true);
            response.put("message", "User disconnected successfully.");
        } else {
            response.put("success", false);
            response.put("message", "No active session found for this MAC address.");
        }

        return response;
    }



}

