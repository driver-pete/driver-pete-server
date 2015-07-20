package com.otognan.driverpete.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.social.SocialWebAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableAutoConfiguration(exclude = { SocialWebAutoConfiguration.class })
@Configuration
@ComponentScan
public class StatelessAuthentication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(StatelessAuthentication.class, args);
	}

	/*
	 * This method together with inheriting SpringBootServletInitializer makes the
	 * application class support Servlet 3.0 interfaces so that it can be executed
	 * in the external servlet container (e.g. Tomcat provided by Amazon)
	*/
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(StatelessAuthentication.class);
    }
}
