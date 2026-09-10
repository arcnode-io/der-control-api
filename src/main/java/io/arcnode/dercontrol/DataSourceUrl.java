package io.arcnode.dercontrol;

import java.net.URI;
import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Parses the platform's libpq-style connection URL ({@code postgres://user:pw@host:port/db}, same
 * shape as {@code DOCUMENT_URL} / {@code VECTOR_URL} on the other services) into what Spring/Hikari
 * actually take — a {@code jdbc:} URL plus separate username/password. Hikari doesn't accept
 * embedded userinfo, so this split has to happen somewhere; here beats a properties file.
 *
 * @param jdbcUrl {@code jdbc:postgresql://host:port/db}
 * @param username the libpq URL's userinfo, before the {@code :}
 * @param password the libpq URL's userinfo, after the {@code :}
 */
public record DataSourceUrl(String jdbcUrl, String username, String password) {

  /**
   * @param libpqUrl e.g. {@code postgres://user:pw@host:5432/db} — scheme may be {@code postgres}
   *     or {@code postgresql}, both map to the {@code jdbc:postgresql:} driver.
   * @throws IllegalArgumentException if the URL carries no {@code user:password} userinfo
   */
  public static DataSourceUrl parse(String libpqUrl) {
    URI uri = URI.create(libpqUrl);
    String userInfo = uri.getUserInfo();
    if (userInfo == null || !userInfo.contains(":")) {
      throw new IllegalArgumentException(
          "DER_CONTROL_URL missing user:password userinfo: " + libpqUrl);
    }
    int sep = userInfo.indexOf(':');
    String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
    return new DataSourceUrl(jdbcUrl, userInfo.substring(0, sep), userInfo.substring(sep + 1));
  }

  /**
   * Reads {@code DER_CONTROL_URL} (libpq shape, set by platform-api in beta/cloud) and, when
   * present, splits it via {@link DataSourceUrl} into {@code spring.datasource.*} properties. Local
   * dev / CI has no {@code DER_CONTROL_URL} — this is a no-op there and {@code application.yml}'s
   * {@code postgresHost} + {@code POSTGRES_PASSWORD} defaults apply instead.
   *
   * <p>Registered in {@code META-INF/spring.factories} as {@code DataSourceUrl$Loader}, alongside
   * {@code Config$Loader}.
   */
  public static class Loader implements EnvironmentPostProcessor {

    private static final String ENV_VAR = "DER_CONTROL_URL";
    private static final String PROPERTY_SOURCE_NAME = "der-control-url";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication app) {
      String url = environment.getProperty(ENV_VAR);
      if (url == null || url.isBlank()) {
        return;
      }
      DataSourceUrl parsed = parse(url);
      Map<String, Object> props =
          Map.of(
              "spring.datasource.url", parsed.jdbcUrl(),
              "spring.datasource.username", parsed.username(),
              "spring.datasource.password", parsed.password());
      // Reason: addFirst so DER_CONTROL_URL beats the application.yml local-dev defaults, same
      // precedence trick Config.Loader uses for cfg.yml.
      environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, props));
    }
  }
}
