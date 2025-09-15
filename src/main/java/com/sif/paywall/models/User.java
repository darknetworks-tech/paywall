package com.sif.paywall.models;

public class User {
    private int id;
    private String username;
    private String email;
    private double balance;
    private String password;
    public User() {
    }
    public User(int id, String username, String email, double balance) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.balance = balance;

    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username=username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email=email;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getPassword() {
        return password;
    }
}
