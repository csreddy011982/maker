package com.dvtech.maker;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MakerApplication {

	@Autowired
	public static void main(String[] args) {
		SpringApplication.run(MakerApplication.class, args);


	}
}
