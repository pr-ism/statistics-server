package com.prism.statistics.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.prism.statistics.domain.user.NicknameGenerator;
import com.prism.statistics.global.config.properties.NicknameProperties;
import java.time.Clock;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(NicknameProperties.class)
public class AppConfig implements WebMvcConfigurer {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private final NicknameProperties nicknameProperties;

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.simpleDateFormat(DATE_TIME_FORMAT)
                      .serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                      .build();
    }

    @Bean
    public NicknameGenerator nicknameGenerator() {
        return NicknameGenerator.of(nicknameProperties.adjective(), nicknameProperties.color());
    }
}
