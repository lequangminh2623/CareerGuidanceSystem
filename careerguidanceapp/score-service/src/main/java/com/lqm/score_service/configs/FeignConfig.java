package com.lqm.score_service.configs;

import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.ContentType;
import feign.form.MultipartFormContentProcessor;
import feign.form.spring.SpringFormEncoder;
import feign.form.spring.SpringManyMultipartFilesWriter;
import feign.form.spring.SpringSingleMultipartFileWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.JsonFormWriter;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    private final ObjectProvider<HttpMessageConverter<?>> messageConverters;
    private final ObjectProvider<HttpMessageConverterCustomizer> customizers;

    @Bean
    public FeignHttpMessageConverters feignHttpMessageConverters() {
        return new FeignHttpMessageConverters(messageConverters, customizers);
    }

    @Bean
    JsonFormWriter jsonFormWriter() {
        return new JsonFormWriter();
    }


    @Bean
    public Encoder feignEncoder(JsonFormWriter jsonFormWriter, ObjectProvider<FeignHttpMessageConverters> converters) {
        SpringEncoder springEncoder = new SpringEncoder(converters);
        SpringFormEncoder formEncoder = new SpringFormEncoder(springEncoder);
        MultipartFormContentProcessor processor = (MultipartFormContentProcessor) formEncoder.getContentProcessor(ContentType.MULTIPART);

        processor.addFirstWriter(jsonFormWriter);
        processor.addFirstWriter(new SpringSingleMultipartFileWriter());
        processor.addFirstWriter(new SpringManyMultipartFilesWriter());

        return formEncoder;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}

