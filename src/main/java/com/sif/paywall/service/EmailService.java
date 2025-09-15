package com.sif.paywall.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private static JavaMailSender mailSender;


    public void sendPasswordToUser(String toemail, String plainPassword, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sfitechteam@gmail.com"); // your Gmail
            message.setTo(toemail);
            message.setSubject("Your Hotspot Login Credentials");
            message.setText("Hello " + username + ",\n\n" +
                    "Your account has been created successfully.\n\n" +
                    "Here are your login details:\n" +
                    "Username: " + username + "\n" +
                    "Password: " + plainPassword + "\n\n" +
                    "Use these credentials to log into the hotspot.\n\n" +
                    "Best regards,\n" +
                    "SFI Tech Team");

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to " + toemail);
        } catch (Exception e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
        }

    }
}
