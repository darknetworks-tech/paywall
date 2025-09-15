package com.sif.paywall.models;

public class UserDTO {
    private String username;
    public static String password;
    private String email;
    private String hashedPassword;
    private double balance;


    public UserDTO() {}
    public  String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public static String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String gethashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public double getbalance() {
        return balance;
    }

}
