package com.codereview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodeReviewDashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeReviewDashboardApplication.class, args);
    }
}
