package com.hireconnect.jobservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JobServiceApplicationTests {

    @Test
    void contextLoads() {
        // Just verify context starts
    }

    @Test
    void main() {
        // Call main for coverage
        JobServiceApplication.main(new String[] {});
    }
}
