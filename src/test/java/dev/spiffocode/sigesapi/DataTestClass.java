package dev.spiffocode.sigesapi;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.common.infrastructure.config.AuditingConfig;
import dev.spiffocode.sigesapi.common.infrastructure.config.S3Properties;
import dev.spiffocode.sigesapi.common.infrastructure.config.SecurityAuditorAware;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@UnitTestClass
@Import({AuditingConfig.class, SecurityAuditorAware.class, SecurityContextHelper.class, TestcontainersConfiguration.class, S3Properties.class})
@WithMockUser("user@example.com")
@Transactional
@DataJpaTest
public @interface DataTestClass {
}
