package com.Rently;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootApplication
public class RentlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentlyApplication.class, args);
	}

	// === DIAGNÓSTICO DE MAIL (temporal) ===
	@Bean
	CommandLineRunner mailDiag(JavaMailSender sender) {
		return args -> {
			if (sender instanceof org.springframework.mail.javamail.JavaMailSenderImpl s) {
				System.out.println("[MAIL] host=" + s.getHost()
						+ " port=" + s.getPort()
						+ " username=" + s.getUsername());
			}
		};
	}
}
