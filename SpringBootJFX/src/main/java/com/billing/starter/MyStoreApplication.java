package com.billing.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.billing.main.MyStoreFxSplash;
@ComponentScan("com.billing")
@SpringBootApplication
public class MyStoreApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(MyStoreApplication.class);
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(MyStoreApplication.class);
		app.run(args);
		app.setHeadless(false);
		System.out.println("Test");
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Hello !!");
		logger.info("this is a info message");
	      logger.warn("this is a warn message");
	      logger.error("this is a error message");
	      MyStoreFxSplash.launch();
	}

}
