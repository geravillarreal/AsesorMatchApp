package com.uanl.asesormatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootApplication
public class AsesormatchApplication {

	private static final Logger logger = LogManager.getLogger(AsesormatchApplication.class);

	public static void main(String[] args) {
		logger.info("Starting AsesorMatch application");
		SpringApplication.run(AsesormatchApplication.class, args);
	}

}
