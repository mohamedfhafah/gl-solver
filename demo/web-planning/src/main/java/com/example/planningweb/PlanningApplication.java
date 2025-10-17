package com.example.planningweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application Spring Boot servant d'exemple pour exposer le solver GL via une API REST.
 */
@SpringBootApplication
public class PlanningApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanningApplication.class, args);
    }
}
