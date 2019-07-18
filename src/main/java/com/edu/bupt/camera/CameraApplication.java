package com.edu.bupt.camera;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.edu.bupt.camera.dao")
public class CameraApplication {
    public static void main(String[] args) {
        SpringApplication.run(CameraApplication.class, args);
    }
}
