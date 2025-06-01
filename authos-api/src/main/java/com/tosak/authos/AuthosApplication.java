package com.tosak.authos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableCaching
public class AuthosApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthosApplication.class, args);
	}

}
