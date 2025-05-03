package com.rsupport.pretest.kyusubkim.common.exception

import org.springframework.http.HttpStatus

data class RSupportBadRequestException(
    override val message: String,
    val code: String = HttpStatus.BAD_REQUEST.name,
) : RuntimeException(message)