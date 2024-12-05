package ru.pyatkinmv.where_to_go;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class WhereToGoApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhereToGoApplication.class, args);
	}

}
