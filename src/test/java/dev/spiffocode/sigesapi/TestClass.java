package dev.spiffocode.sigesapi;

import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@ActiveProfiles("test")
public @interface TestClass {
}
