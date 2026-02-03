package dev.codewave.sigesapi;

import org.springframework.boot.SpringApplication;

public class TestSigesApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(SigesApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
