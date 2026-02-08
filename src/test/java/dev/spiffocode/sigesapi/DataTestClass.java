package dev.spiffocode.sigesapi;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@UnitTestClass
@Import(TestcontainersConfiguration.class)
@Transactional
@DataJpaTest
public @interface DataTestClass {
}
