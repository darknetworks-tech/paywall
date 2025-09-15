package com.sif.paywall.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaystackService {

    @Value("${paystack.secret.key}")
    private String secretKey;

    @Value("${paystack.base.url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ Initialize payment
    public Map<String, Object> initializePayment(String email, double amount, String planName, Map<String, Object> metadata) {
        String url = baseUrl + "/transaction/initialize";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("amount", (int)(amount * 100)); // Paystack expects amount in Kobo
        body.put("metadata", Map.of("plan", planName));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        return response.getBody();
    }

    // ✅ Verify payment
    public Map<String, Object> verifyPayment(String reference) {
        String url = baseUrl + "/transaction/verify/" + reference;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }
}

