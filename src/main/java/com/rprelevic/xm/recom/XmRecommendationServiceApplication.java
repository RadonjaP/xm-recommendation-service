package com.rprelevic.xm.recom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class XmRecommendationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(XmRecommendationServiceApplication.class, args);
	}

}
