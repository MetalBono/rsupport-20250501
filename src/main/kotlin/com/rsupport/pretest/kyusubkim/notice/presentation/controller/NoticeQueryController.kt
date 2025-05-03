package com.rsupport.pretest.kyusubkim.notice.presentation.controller

import com.rsupport.pretest.kyusubkim.common.util.getContentTypeByFileExtension
import com.rsupport.pretest.kyusubkim.notice.application.usecase.FileQueryUseCase
import com.rsupport.pretest.kyusubkim.notice.application.usecase.NoticeQueryUseCase
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeSearchType
import com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto.NoticeListResponse
import com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto.NoticeResponse
import com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto.toResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Tag(name = "공지사항 (서비스용)")
@RestController
@RequestMapping("/api/v1/notice")
class NoticeQueryController(
    private val fileQueryUseCase: FileQueryUseCase,
    private val noticeQueryUseCase: NoticeQueryUseCase,
) {
    @Operation(summary = "공지사항 조회 API", description = "notice-id 에 해당하는 공지사항을 조회합니다.")
    @GetMapping("/{notice-id}")
    fun getNotice(
        @PathVariable("notice-id") noticeId: Long,
    ): NoticeResponse = noticeQueryUseCase.getNotice(noticeId).toResponse()

    @Operation(summary = "공지사항 목록 조회 API", description = "조건에 부합하는 공지사항 목록을 조회합니다.")
    @GetMapping("/list")
    fun getNoticeList(
        @Parameter(description = "검색 기준", required = false)
        @RequestParam(value = "searchType", required = false) searchType: NoticeSearchType?,
        @Parameter(description = "검색어", required = false)
        @RequestParam(value = "searchKeyword", required = false) searchKeyword: String?,
        @Parameter(description = "페이지 크기", example = "30")
        @RequestParam(value = "pageSize") pageSize: Int,
        @Parameter(description = "페이지 커서 (이전 페이지의 마지막 요소 id 값. 첫 페이지의 경우 null)", required = false)
        @RequestParam(value = "cursor", required = false) cursor: Long?,
    ): NoticeListResponse {
        return noticeQueryUseCase.getNoticeList(searchType, searchKeyword, pageSize, cursor).toResponse()
    }

    @Operation(summary = "공지사항 첨부파일 다운로드 API", description = "file-id 에 해당하는 첨부파일을 다운로드합니다.")
    @GetMapping("/attachment/{file-id}")
    fun downloadFile(
        @PathVariable("file-id") fileId: Long,
    ): ResponseEntity<ByteArray> {
        val file = fileQueryUseCase.getAttachedFile(fileId)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${URLEncoder.encode(file.name, StandardCharsets.UTF_8)}\"")
            .contentType(getContentTypeByFileExtension(file.name))
            .body(file.fileData)
    }
}