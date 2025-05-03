package com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공지사항 첨부파일 정보 응답")
data class NoticeAttachmentResponse(
    @Schema(description = "파일 경로", example = "https://localhost:8080/api/v1/notice/attachment/1")
    val filePath: String?,
)