package com.example.library;
//льижжлпьидп
import com.example.library.config.ReservationProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
@EnableConfigurationProperties(ReservationProperties.class)
public class LibraryApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Bishkek"));
    }

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);

    }

}
