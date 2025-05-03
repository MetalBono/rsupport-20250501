package com.rsupport.pretest.kyusubkim.common.exception

import org.springframework.http.HttpStatus

data class RSupportException(
    override val message: String,
    val code: String = HttpStatus.INTERNAL_SERVER_ERROR.name,
) : RuntimeException(message)