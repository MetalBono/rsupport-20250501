package com.rsupport.pretest.kyusubkim

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableScheduling
@SpringBootApplication
class KyusubkimApplication

fun main(args: Array<String>) {
	runApplication<KyusubkimApplication>(*args)
}
