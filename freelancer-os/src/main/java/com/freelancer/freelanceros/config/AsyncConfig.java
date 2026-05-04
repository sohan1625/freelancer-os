package com.freelancer.freelanceros.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // @EnableAsync      — @Async on EmailService sends emails in background threads
    // @EnableScheduling — @Scheduled on InvoiceScheduler runs daily overdue + reminder jobs
}