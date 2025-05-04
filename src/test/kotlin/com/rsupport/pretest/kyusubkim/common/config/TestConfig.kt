package com.rsupport.pretest.kyusubkim.common.config

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import redis.embedded.RedisServer

@TestConfiguration
class TestConfig {
    private var redisServer: RedisServer = RedisServer(REDIS_PORT)

    @PostConstruct
    fun startRedis() {
        redisServer.start()
    }

    @PreDestroy
    fun stopRedis() {
        redisServer.stop()
    }

    @Bean(destroyMethod = "shutdown")
    fun redissonClient(): RedissonClient {
        val host = "localhost"
        val port = REDIS_PORT

        val config = Config().apply {
            useSingleServer()
                .setAddress("redis://$host:$port")
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(2)
        }

        return Redisson.create(config)
    }

    companion object {
        const val REDIS_PORT = 6379
    }
}