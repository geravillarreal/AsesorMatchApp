package com.uanl.asesormatch.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@TestConfiguration
@EntityScan("com.uanl.asesormatch.entity")
public class TestEntityScanConfig {
}
