package dev.spiffocode.sigesapi;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@UnitTestClass
@DataJpaTest
public @interface DataTestClass {
}
