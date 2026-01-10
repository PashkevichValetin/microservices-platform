package com.pashcevich.data_unifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataUnifierApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataUnifierApplication.class, args);
	}
}
