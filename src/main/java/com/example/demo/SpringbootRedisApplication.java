package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@SpringBootApplication(scanBasePackages={"com.example.demo"})
@MapperScan(basePackages = {"com.example.demo.dao"})
public class SpringbootRedisApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootRedisApplication.class, args);
	}


	@Bean
	public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){

	    RedisTemplate redisTemplate = new RedisTemplate();
	    redisTemplate.setConnectionFactory(redisConnectionFactory);
	    redisTemplate.setKeySerializer(new Jackson2JsonRedisSerializer(Object.class));
	    redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(Object.class));
	    redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();
	    return redisTemplate;
    }


}

