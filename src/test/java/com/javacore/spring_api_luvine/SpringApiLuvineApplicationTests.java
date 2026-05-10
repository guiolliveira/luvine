package com.javacore.spring_api_luvine;

import com.javacore.spring_api_luvine.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SpringApiLuvineApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
	}

}