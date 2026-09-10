package io.arcnode.dercontrol;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Shared real Postgres for every {@code *IT}. {@code @ServiceConnection} auto-populates {@code
 * spring.datasource.*}, so tests never touch connection strings. The container is a singleton for
 * the JVM — one startup for the whole integration suite.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer("postgres:16-alpine");
  }
}
