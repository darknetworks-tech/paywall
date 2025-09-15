package com.sif.paywall.models;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

@Component
public class ConnectDB {
    private static Connection connection;

    public ConnectDB() {
        String url = "jdbc:sqlite:mydb.db";
        try {
            connection = DriverManager.getConnection(url);
            enableForeignKeys(connection);
            createTables(connection);
            System.out.println("Connection Successful");
        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        if (connection != null) {
            System.out.println("Connection Successful");
            this.connection = connection;
        } else {
            System.out.println("Connection Failed");
        }
    }

    private void enableForeignKeys(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }

    private void createTables(Connection conn) {
        String[] sqlStatements = {
                // Users
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "email TEXT NOT NULL UNIQUE, " +
                        "balance REAL DEFAULT 0);",

                // Plans
                "CREATE TABLE IF NOT EXISTS plans (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "data_limit_mb INTEGER, " +
                        "duration_days INTEGER, " +
                        "price REAL NOT NULL);",

                // Subscriptions
                "CREATE TABLE IF NOT EXISTS subscriptions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "plan_id INTEGER NOT NULL, " +
                        "start_date DATE, " +
                        "end_date DATE, " +
                        "status TEXT CHECK(status IN ('active','expired','cancelled')), " +
                        "FOREIGN KEY (user_id) REFERENCES users(id), " +
                        "FOREIGN KEY (plan_id) REFERENCES plans(id));",

                // Connections
                "CREATE TABLE IF NOT EXISTS connections (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER, " +
                        "mac_address TEXT, " +
                        "ip_address TEXT, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "data_used_mb REAL, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id));",

                // Payments
                "CREATE TABLE IF NOT EXISTS payments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "amount REAL NOT NULL, " +
                        "plan_id INTEGER NOT NULL, " +
                        "start_date DATE NOT NULL, " +
                        "end_date DATE NOT NULL, " +
                        "status TEXT CHECK(status IN ('active','expired','cancelled')), " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "transaction_id TEXT, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id), " +
                        "FOREIGN KEY (plan_id) REFERENCES plans(id));",

                // Payment Methods
                "CREATE TABLE IF NOT EXISTS payment_methods (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "details TEXT NOT NULL, " +
                        "is_default BOOLEAN NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id));",

                // Invoices
                "CREATE TABLE IF NOT EXISTS invoices (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "amount REAL NOT NULL, " +
                        "due_date DATE NOT NULL, " +
                        "paid_date DATE, " +
                        "status TEXT CHECK(status IN ('unpaid','paid','overdue')), " +
                        "FOREIGN KEY (user_id) REFERENCES users(id));"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : sqlStatements) {
                stmt.executeUpdate(sql);
            }
            System.out.println("Tables created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    // ✅ Insert user
    public boolean insertUser(String username, String password, String email, String s, double balance) {
        String sql = "INSERT INTO users (username, password, email, balance) VALUES (?, ?, ?, ?)";

        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setDouble(4, balance);
            pstmt.executeUpdate();
            System.out.println("User inserted successfully.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }

    // ✅ Check if email exists
    public boolean emailExists(String email) {
        String sqlstmt = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (var pstmt = connection.prepareStatement(sqlstmt)) {
            pstmt.setString(1, email);
            var rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
            return false;
        }
    }

    public int findUserIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection connection = ConnectDB.getConnection();

             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return -1;
    }
    public String getUsernameById(int userId) {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection connection = ConnectDB.getConnection();

             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            System.err.println("Error getting username: " + e.getMessage());
        }
        return null;
    }
    public String getPasswordById(int userId) {
        String sql = "SELECT password FROM users WHERE id = ?";
        try (Connection connection = ConnectDB.getConnection();

             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("password");
        } catch (SQLException e) {
            System.err.println("Error getting password: " + e.getMessage());
        }
        return null;
    }
    public void activateUserSubscription(int userId, int durationDays) {
        String sql = "INSERT INTO subscriptions (user_id, plan_id, start_date, end_date, status) " +
                "VALUES (?, 1, date('now'), date('now', '+' || ? || ' days'), 'active')";
        try (Connection connection = ConnectDB.getConnection();

             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, durationDays);
            pstmt.executeUpdate();
            System.out.println("Subscription activated for user " + userId + " (" + durationDays + " days)");
        } catch (SQLException e) {
            System.err.println("Error activating subscription: " + e.getMessage());
        }
    }
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }


    public boolean insertUser(String username, String hashedPassword, String email, double v) {
    }

    public User getUserByEmail(String email) {
    }

    public boolean updateUserPassword(int id, String newHashedPassword, String newPassword) {
    }
}
