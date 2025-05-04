package com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Schema(description = "공지사항 등록 요청")
data class CreateNoticeRequest(
    @Schema(description = "제목", example = "공지사항 제목입니다.")
    @field:NotBlank(message = "제목은 필수 값 입니다.")
    @field:Size(min = 1, max = 100, message = "제목은 1자 ~ 100자 이내로 작성해주세요.")
    val title: String?,
    @Schema(description = "내용", example = "공지사항 내용입니다.")
    @field:NotBlank(message = "내용은 필수 값 입니다.")
    @field:Size(min = 1, max = 2000, message = "내용은 1자 ~ 2000자 이내로 작성해주세요.")
    val content: String?,
    @Schema(description = "시작 일시", example = "2025-05-01 10:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:NotNull(message = "공지 시작 일시 값은 필수 값 입니다.")
    val startsAt: LocalDateTime?,
    @Schema(description = "종료 일시", example = "2025-05-10 23:59:59")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:NotNull(message = "공지 종료 일시 값은 필수 값 입니다.")
    val endsAt: LocalDateTime?,
    @Schema(description = "첨부파일 목록")
    val attachments: List<CreateNoticeAttachmentRequest>? = null,
    @Schema(description = "등록자", example = "12345")
    @field:NotBlank(message = "등록자는 필수 값 입니다.")
    val createdBy: String?,
)
