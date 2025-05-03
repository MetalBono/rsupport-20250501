package com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "공지사항 응답")
data class NoticeResponse(
    @Schema(description = "공지사항 ID", example = "1")
    val id: Long,
    @Schema(description = "제목", example = "공지사항 제목입니다.")
    val title: String,
    @Schema(description = "내용", example = "공지사항 내용입니다.")
    val content: String,
    @Schema(description = "시작 일시", example = "2025-05-01 10:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val startsAt: LocalDateTime,
    @Schema(description = "종료 일시", example = "2025-05-10 23:59:59")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val endsAt: LocalDateTime,
    @Schema(description = "조회 수", example = "382")
    val viewCount: Long,
    @Schema(description = "첨부파일 목록")
    val attachments: List<NoticeAttachmentResponse> = emptyList(),
    @Schema(description = "등록 일시", example = "2025-04-23 16:43:59")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
    @Schema(description = "등록자", example = "12345")
    val createdBy: String,
    @Schema(description = "수정 일시", example = "2025-04-28 11:37:12")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime? = null,
    @Schema(description = "수정자", example = "23456")
    val updatedBy: String? = null,
) {
}