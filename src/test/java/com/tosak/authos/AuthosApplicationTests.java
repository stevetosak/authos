package com.tosak.authos;

import com.tosak.authos.service.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.interfaces.RSAKey;

@SpringBootTest
class AuthosApplicationTests {

    @Autowired
    private JwtUtils jwtUtils;
	@Autowired
	private RSAKey rsaKeyPair;

	@Test
	void contextLoads() {
	}

	@Test
	void jwtGenerationTest(){
		String jwt = jwtUtils.createSignedJwt(1);
		System.out.println("BEARER: " + jwt);
	}

}
