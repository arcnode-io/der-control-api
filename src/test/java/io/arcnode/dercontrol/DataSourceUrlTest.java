package io.arcnode.dercontrol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

/** Unit — libpq URL → JDBC url + credentials, and the env-var loader. AAA. */
class DataSourceUrlTest {

  @Test
  void parsesPostgresScheme() {
    // Arrange
    String url = "postgres://ems_dercontrol_app:s3cr3t@aurora-endpoint:5432/ems_dercontrol";

    // Act
    DataSourceUrl parsed = DataSourceUrl.parse(url);

    // Assert
    assertThat(parsed.jdbcUrl()).isEqualTo("jdbc:postgresql://aurora-endpoint:5432/ems_dercontrol");
    assertThat(parsed.username()).isEqualTo("ems_dercontrol_app");
    assertThat(parsed.password()).isEqualTo("s3cr3t");
  }

  @Test
  void parsesPostgresqlScheme() {
    // Arrange: appliance shape — postgresql:// instead of postgres://
    String url = "postgresql://arcnode:pw@postgres-document:5432/dercontrol";

    // Act
    DataSourceUrl parsed = DataSourceUrl.parse(url);

    // Assert
    assertThat(parsed.jdbcUrl()).isEqualTo("jdbc:postgresql://postgres-document:5432/dercontrol");
    assertThat(parsed.username()).isEqualTo("arcnode");
    assertThat(parsed.password()).isEqualTo("pw");
  }

  @Test
  void rejectsUrlWithoutCredentials() {
    // Arrange
    String url = "postgres://aurora-endpoint:5432/ems_dercontrol";

    // Act / Assert
    assertThatThrownBy(() -> DataSourceUrl.parse(url)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void loaderNoOpsWhenEnvVarUnset() {
    // Arrange: local/dev — cfg.yml postgresHost + POSTGRES_PASSWORD defaults apply instead
    MockEnvironment env = new MockEnvironment();

    // Act
    new DataSourceUrl.Loader().postProcessEnvironment(env, new SpringApplication());

    // Assert
    assertThat(env.getProperty("spring.datasource.url")).isNull();
  }

  @Test
  void loaderSetsDatasourcePropertiesWhenEnvVarPresent() {
    // Arrange
    MockEnvironment env =
        new MockEnvironment()
            .withProperty("DER_CONTROL_URL", "postgres://ems_app:pw@aurora-host:5432/ems_db");

    // Act
    new DataSourceUrl.Loader().postProcessEnvironment(env, new SpringApplication());

    // Assert
    assertThat(env.getProperty("spring.datasource.url"))
        .isEqualTo("jdbc:postgresql://aurora-host:5432/ems_db");
    assertThat(env.getProperty("spring.datasource.username")).isEqualTo("ems_app");
    assertThat(env.getProperty("spring.datasource.password")).isEqualTo("pw");
  }
}
