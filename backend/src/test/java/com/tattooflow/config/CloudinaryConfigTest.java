package com.tattooflow.config;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CloudinaryConfigTest {

    @Test
    void deveCriarBeanCloudinaryComPropriedadesConfiguradas() {
        CloudinaryConfig config = new CloudinaryConfig();
        ReflectionTestUtils.setField(config, "cloudName", "teste_cloud");
        ReflectionTestUtils.setField(config, "apiKey", "teste_key");
        ReflectionTestUtils.setField(config, "apiSecret", "teste_secret");

        Cloudinary cloudinary = config.cloudinary();

        assertThat(cloudinary).isNotNull();
        assertThat(cloudinary.config.cloudName).isEqualTo("teste_cloud");
        assertThat(cloudinary.config.apiKey).isEqualTo("teste_key");
        assertThat(cloudinary.config.apiSecret).isEqualTo("teste_secret");
    }
}

