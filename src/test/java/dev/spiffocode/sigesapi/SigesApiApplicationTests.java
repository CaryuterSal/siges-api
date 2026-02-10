package dev.spiffocode.sigesapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SigesApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
