package com.sif.paywall.controller;

import com.sif.paywall.service.MikroTikService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mikrotik")
public class MikroTikController {

    private final MikroTikService mikroTikService;

    public MikroTikController(MikroTikService mikroTikService) {
        this.mikroTikService = mikroTikService;
    }

    @GetMapping("/active-users")
    public List<Map<String, String>> getActiveUsers() {
        return mikroTikService.getActiveUsers();
    }
    @PostMapping("/disconnect/{username}")
    public String disconnectUser(@PathVariable String username) {
        mikroTikService.disconnectUser(username);
        return "User " + username + " has been disconnected.";
    }
}

