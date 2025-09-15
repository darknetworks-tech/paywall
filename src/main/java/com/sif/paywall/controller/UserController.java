package com.sif.paywall.controller;

import com.sif.paywall.models.ConnectDB;
import com.sif.paywall.models.User;
import com.sif.paywall.service.EmailService;
import com.sif.paywall.service.MikroTikService;
import com.sif.paywall.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private ConnectDB connectDB;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MikroTikService mikroTikService;

    // ✅ Register User
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestParam String username,
                                                            @RequestParam String password,
                                                            @RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (connectDB.emailExists(email)) {
                response.put("success", false);
                response.put("message", "Email already in use.");
                return ResponseEntity.badRequest().body(response);
            }

            String hashedPassword = passwordEncoder.encode(password);

            boolean success = connectDB.insertUser(
                    username,
                    hashedPassword,
                    password, // store plain password too (for MikroTik)
                    email,
                    0.0
            );

            if (success) {
                // Add user to MikroTik immediately
                mikroTikService.createHotspotUser(username, password, "default");

                // Email user their credentials
                emailService.sendPasswordToUser(email, username, password);

                response.put("success", true);
                response.put("message", "User registered successfully. Password sent to email.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Error registering user.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ✅ Login User
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestParam String email,
                                                         @RequestParam String password) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connectDB.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");

                if (BCrypt.checkpw(password, storedHash)) {
                    int userId = rs.getInt("id");
                    String token = JwtUtil.generateToken(rs.getString("email"), userId);

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Login successful.");
                    response.put("token", token);
                    response.put("userId", userId);
                    response.put("username", rs.getString("username"));

                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(401).body(Map.of(
                            "success", false,
                            "message", "Invalid password."
                    ));
                }
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "User not found."
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    // ✅ Get User
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable int id,
                                     @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Missing or invalid token."
            ));
        }

        String token = authHeader.substring(7);

        if (!JwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Invalid or expired token."
            ));
        }

        String sql = "SELECT id, username, email, balance FROM users WHERE id = ?";
        try (var conn = connectDB.getConnection();
             var pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getDouble("balance")
                );
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "User not found."
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error fetching user: " + e.getMessage()
            ));
        }
    }

    // ✅ Change Password
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestParam String email,
                                                              @RequestParam String oldPassword,
                                                              @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = connectDB.getUserByEmail(email);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found.");
                return ResponseEntity.badRequest().body(response);
            }

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                response.put("success", false);
                response.put("message", "Old password is incorrect.");
                return ResponseEntity.badRequest().body(response);
            }

            String newHashedPassword = passwordEncoder.encode(newPassword);

            boolean updated = connectDB.updateUserPassword(user.getId(), newHashedPassword, newPassword);

            if (updated) {
                // Update MikroTik user password
                mikroTikService.updateHotspotPassword(user.getUsername(), newPassword);

                // Email updated password
                emailService.sendPasswordToUser(user.getEmail(), user.getUsername(), newPassword);

                response.put("success", true);
                response.put("message", "Password updated successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Error updating password.");
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ✅ Check if email exists
    @GetMapping("/check")
    public String checkEmail(@RequestParam String email) {
        boolean exists = connectDB.emailExists(email);
        return exists ? "Email already exists" : "Email is available";
    }
}
