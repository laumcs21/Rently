package com.Rently.Business.Integration;

import com.Rently.RentlyApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = RentlyApplication.class)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
}
