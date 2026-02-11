package dev.spiffocode.sigesapi.integration;

import dev.spiffocode.sigesapi.SigesApiApplication;
import dev.spiffocode.sigesapi.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestSigesApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(SigesApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
