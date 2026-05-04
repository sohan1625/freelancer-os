package com.freelancer.freelanceros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.freelancer.freelanceros") // 🔥 ADD THIS
@EnableScheduling
public class FreelancerOsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FreelancerOsApplication.class, args);
	}
}