package com.rsupport.pretest.kyusubkim.common.exception.handler

import com.rsupport.pretest.kyusubkim.common.exception.RSupportBadRequestException
import com.rsupport.pretest.kyusubkim.common.exception.RSupportErrorResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class RSupportExceptionHandler {

    @ExceptionHandler(RSupportBadRequestException::class)
    fun handleBadRequestException(ex: RSupportBadRequestException): ResponseEntity<Any> {
        return ResponseEntity<Any>(
            RSupportErrorResult(
                message = ex.message,
                code = ex.code,
            ),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<Any> {
        val firstMessage = ex.allErrors.first().defaultMessage
        return ResponseEntity<Any>(
            RSupportErrorResult(
                message = firstMessage,
                code = HttpStatus.BAD_REQUEST.name,
            ),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(ex: MissingServletRequestParameterException): ResponseEntity<Any> {
        val name = ex.parameterName
        return ResponseEntity<Any>(
            RSupportErrorResult(
                message = "필수 요청 파라미터 '$name'이(가) 누락되었습니다.",
                code = HttpStatus.BAD_REQUEST.name,
            ),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(Exception::class)
    fun handlerException(ex: Exception): ResponseEntity<Any> {
        return ResponseEntity<Any>(
            RSupportErrorResult(
                message = ex.message,
                code = HttpStatus.INTERNAL_SERVER_ERROR.name
            ),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }
}