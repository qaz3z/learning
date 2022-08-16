package com.mp.demo.config;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <b>功能描述:Redisson的配置</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-13
 * @since JDK 1.8
 */
@Configuration
public class RedissonConfig {

    @Value("${redisson.redis.host}")
    private String host;
    @Value("${redisson.redis.port}")
    private String port;
    @Value("${redisson.redis.password}")
    private String password;

    @Bean
    public Config redissionConfig() {
        Config config = new Config();
        String redisUrl = String.format("redis://%s:%s", host, port);
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(redisUrl);

        if (StringUtils.isNotEmpty(password)) {
            singleServerConfig.setPassword(password);
        }
        return config;
    }

    @Bean
    public RedissonClient redissonClient() {
        return Redisson.create(redissionConfig());
    }
}
