package com.windmealchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class WindmealchatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WindmealchatApplication.class, args);
    }

}
