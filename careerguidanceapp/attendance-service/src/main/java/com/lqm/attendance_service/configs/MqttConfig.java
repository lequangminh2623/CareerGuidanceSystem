package com.lqm.attendance_service.configs;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
@Slf4j
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.tls.enabled}")
    private boolean tlsEnabled;

    @Value("${mqtt.tls.truststore.path}")
    private String truststorePath;

    @Value("${mqtt.tls.truststore.password}")
    private String truststorePassword;

    @Value("${mqtt.tls.keystore.path}")
    private String keystorePath;

    @Value("${mqtt.tls.keystore.password}")
    private String keystorePassword;

    @Value("${mqtt.tls.keystore.type}")
    private String keystoreType;

    @Bean
    public MqttClient mqttClient() throws Exception {
        // Tạo Client ID cố định hoặc động tùy nhu cầu
        MqttClient client = new MqttClient(brokerUrl, MqttClient.generateClientId());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60); // Thêm cái này để giữ kết nối ổn định

        if (tlsEnabled) {
            configureSSL(options);
            log.info("MQTT mTLS enabled - Connecting to secure broker");
        } else {
            log.warn("MQTT TLS is disabled - using plain connection");
        }

        try {
            client.connect(options);
            log.info("Successfully connected to MQTT broker: {}", brokerUrl);
        } catch (Exception e) {
            log.error("Could not connect to MQTT broker: {}. Error: {}", brokerUrl, e.getMessage());
            // Không nên throw exception dừng app ở đây nếu bạn muốn app vẫn chạy
            // và tự động reconnect sau này. Nhưng tùy logic thesis của bạn nhé.
        }
        return client;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private void configureSSL(MqttConnectOptions options) throws Exception {
        try {
            // Dùng "Default" algorithms để tăng tính tương thích
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            String kmfAlgorithm = KeyManagerFactory.getDefaultAlgorithm();

            KeyStore trustStore = loadKeyStore(truststorePath, truststorePassword, "JKS");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(trustStore);

            KeyStore keyStore = loadKeyStore(keystorePath, keystorePassword, keystoreType);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(kmfAlgorithm);
            kmf.init(keyStore, keystorePassword.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            options.setSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            log.error("SSL Configuration Error: {}", e.getMessage());
            throw e;
        }
    }

    private KeyStore loadKeyStore(String path, String password, String storeType) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(storeType);

        // Cách dùng ResourceLoader của Spring (Gọn hơn)
        org.springframework.core.io.ResourceLoader resourceLoader = new org.springframework.core.io.DefaultResourceLoader();
        org.springframework.core.io.Resource resource = resourceLoader.getResource(path);

        try (InputStream is = resource.getInputStream()) {
            keyStore.load(is, password.toCharArray());
        } catch (Exception e) {
            log.error("Failed to load keystore from path: {}. Error: {}", path, e.getMessage());
            throw e;
        }
        return keyStore;
    }
}