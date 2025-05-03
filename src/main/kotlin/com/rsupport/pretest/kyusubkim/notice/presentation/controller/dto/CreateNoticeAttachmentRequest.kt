package com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "공지사항 첨부파일 정보 등록 요청")
data class CreateNoticeAttachmentRequest(
    @Schema(description = "파일 경로", example = "https://localhost:8080/api/v1/notice/attachment/1")
    @field:NotBlank(message = "파일 경로는 필수 값 입니다.")
    val filePath: String?,
)
