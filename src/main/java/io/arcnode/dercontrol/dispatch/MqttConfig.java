package io.arcnode.dercontrol.dispatch;

import io.arcnode.dercontrol.Config;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Connects to the deployment broker on boot as the {@code arcnode_der_control_api} File-RBAC
 * identity (system_adr §19/§20 — a non-device publisher, same pattern as device-api's {@code
 * system/topology_changed} client). Synchronous connect: if the broker isn't reachable at startup,
 * the app fails to start rather than silently running with no dispatch path.
 */
@Configuration
public class MqttConfig {

  @Bean(destroyMethod = "close")
  public MqttClient mqttClient(
      Config config, @Value("${MQTT_DER_CONTROL_API_PASSWORD:}") String password)
      throws org.eclipse.paho.mqttv5.common.MqttException {
    String clientId = config.mqttUsername() + "-" + UUID.randomUUID();
    MqttClient client = new MqttClient(config.mqttBrokerUrl(), clientId, new MemoryPersistence());

    MqttConnectionOptions options = new MqttConnectionOptions();
    options.setUserName(config.mqttUsername());
    options.setPassword(password.getBytes(StandardCharsets.UTF_8));
    options.setAutomaticReconnect(true);
    client.connect(options);
    return client;
  }
}
