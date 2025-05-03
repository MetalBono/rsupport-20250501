package com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공지사항 목록 응답")
data class NoticeListResponse(
    @Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean,
    @Schema(description = "총 페이지 수", example = "5")
    val totalPages: Int,
    @Schema(description = "총 항목 수", example = "192")
    val totalCount: Long,
    @Schema(description = "공지사항 목록")
    val list: List<NoticeResponse>,
    @Schema(description = "다음 페이지 조회용 cursor", example = "15")
    val nextCursor: Long? = null,
)