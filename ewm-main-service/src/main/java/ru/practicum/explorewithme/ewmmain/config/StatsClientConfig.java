package ru.practicum.explorewithme.ewmmain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.ewmmain.client.StatsClient;

@Configuration
public class StatsClientConfig {
    @Bean
    public StatsClient statsClient() {
        return new StatsClient("http://localhost:9090");
    }
}
