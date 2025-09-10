package com.sif.paywall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.sif.paywall.models.ConnectDB;

import java.sql.*;

@SpringBootApplication
public class PaywallApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaywallApplication.class, args);



    }

}
