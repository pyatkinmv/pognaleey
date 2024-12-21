package ru.pyatkinmv.pognaleey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PognaleeyApp {

	public static void main(String[] args) {
		SpringApplication.run(PognaleeyApp.class, args);
	}

}
