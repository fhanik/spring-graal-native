package com.example.tomcat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(proxyBeanMethods = false)
public class TomcatApplication {

	public static void main(String[] args) {
		final SpringApplication application = new SpringApplication(TomcatApplication.class);
		application.setWebApplicationType(WebApplicationType.SERVLET);
		application.run(args);
	}

}
