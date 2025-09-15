package com.sif.paywall.service;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.ApiConnectionException;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MikroTikService {

    @Value("${mikrotik.host}")
    private String host;

    @Value("${mikrotik.username}")
    private String routerUsername;

    @Value("${mikrotik.password}")
    private String routerPassword;
    private String username;
    private String password;

    public boolean disconnectUser(String username) throws ApiConnectionException {
        ApiConnection con = null;
        try {
            con = ApiConnection.connect(host);
            con.login(routerUsername, routerPassword);

            con.execute("/ip/hotspot/active/remove [find user=\"" + username + "\"]");

            System.out.println("User " + username + " disconnected from hotspot.");
            return true;
        } catch (MikrotikApiException e) {
            System.err.println("API error while disconnecting user: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return false;
    }

    public List<Map<String, String>> getActiveUsers() throws ApiConnectionException {
        List<Map<String, String>> activeUsers = new ArrayList<>();
        ApiConnection con = null;
        try {
            con = ApiConnection.connect(host);
            con.login(routerUsername, routerPassword);

            activeUsers = con.execute("/ip/hotspot/active/print");

        } catch (MikrotikApiException e) {
            System.err.println("API error while fetching active users: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return activeUsers;
    }

    public boolean createHotspotUser(String username, String password, String profile) throws ApiConnectionException {
        ApiConnection con = null;
        try {
            con = ApiConnection.connect(host);
            con.login(routerUsername, routerPassword);

            // Remove existing user if exists
            con.execute("/ip/hotspot/user/remove [find name=\"" + username + "\"]");

            // Add new user with specified profile
            con.execute("/ip/hotspot/user/add name=\"" + username + "\" password=\"" + password + "\" profile=\"" + profile + "\"");

            System.out.println("Hotspot user " + username + " created with profile " + profile);
            return true;
        } catch (MikrotikApiException e) {
            System.err.println("API error while creating user: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return false;
    }
    public static void updateHotspotPassword(String username, String newPassword) {
        try {
            ApiConnection con = ApiConnection.connect(host);
            con.login(this.username, this.password);

            con.execute("/ip/hotspot/user/set [find name=" + username + "] password=" + newPassword);

            con.close();
            System.out.println("Hotspot password updated for " + username);
        } catch (Exception e) {
            System.err.println("Failed to update hotspot password: " + e.getMessage());
        }
    }

}