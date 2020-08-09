package com.academey.book.springboot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @EnableJpaAuditing
// 여기다가 안하고 따로 Confgiruation으로 빼야 WebMvcTest 에서 모델이 등록되지 않는다.
// WebMvcTest 는 @Configuration 을 스캔하지 않기 때문이다.
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
