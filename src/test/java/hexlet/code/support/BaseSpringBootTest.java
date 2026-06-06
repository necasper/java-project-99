package hexlet.code.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class BaseSpringBootTest {

    protected static final String TEST_ADMIN_PASSWORD = "test-admin-password";

    @DynamicPropertySource
    static void configureAdminPassword(DynamicPropertyRegistry registry) {
        registry.add("ADMIN_PASSWORD", () -> TEST_ADMIN_PASSWORD);
    }
}
