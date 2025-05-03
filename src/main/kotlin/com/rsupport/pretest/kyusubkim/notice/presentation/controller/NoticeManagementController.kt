package com.rsupport.pretest.kyusubkim.notice.presentation.controller

import com.rsupport.pretest.kyusubkim.notice.application.usecase.FileManagementUseCase
import com.rsupport.pretest.kyusubkim.notice.application.usecase.NoticeManagementUseCase
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeSearchType
import com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Tag(name = "공지사항 (관리자용)")
@RestController
@RequestMapping("/api/v1/management/notice")
class NoticeManagementController(
    private val fileManagementUseCase: FileManagementUseCase,
    private val noticeManagementUseCase: NoticeManagementUseCase,
) {
    @Operation(summary = "공지사항 등록 API", description = "새 공지사항을 등록합니다.")
    @PostMapping
    fun createNotice(
        @RequestBody @Valid request: CreateNoticeRequest,
    ): NoticeResponse = noticeManagementUseCase.createNotice(request.toDto()).toResponse()

    @Operation(summary = "공지사항 수정 API", description = "notice-id 에 해당하는 공지사항을 수정합니다.")
    @PutMapping("/{notice-id}")
    fun updateNotice(
        @PathVariable("notice-id") noticeId: Long,
        @RequestBody @Valid request: UpdateNoticeRequest,
    ): NoticeResponse = noticeManagementUseCase.updateNotice(request.toDto(noticeId)).toResponse()

    @Operation(summary = "공지사항 삭제 API", description = "notice-id 에 해당하는 공지사항을 삭제합니다.")
    @DeleteMapping("/{notice-id}")
    fun deleteNotice(
        @PathVariable("notice-id") noticeId: Long,
    ): ResponseEntity<Unit> {
        noticeManagementUseCase.deleteNotice(noticeId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

    @Operation(summary = "공지사항 조회 API", description = "notice-id 에 해당하는 공지사항을 조회합니다.")
    @GetMapping("/{notice-id}")
    fun getNotice(
        @PathVariable("notice-id") noticeId: Long,
    ): NoticeResponse = noticeManagementUseCase.getNotice(noticeId).toResponse()

    @Operation(summary = "공지사항 목록 조회 API", description = "조건에 부합하는 공지사항 목록을 조회합니다.")
    @GetMapping("/list")
    fun getNoticeList(
        @Parameter(description = "검색 기준", required = false)
        @RequestParam(value = "searchType", required = false) searchType: NoticeSearchType?,
        @Parameter(description = "검색어", required = false)
        @RequestParam(value = "searchKeyword", required = false) searchKeyword: String?,
        @Parameter(description = "공지사항 등록일 (from)", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @RequestParam(value = "createdAtFrom", required = false) createdAtFrom: LocalDateTime?,
        @Parameter(description = "공지사항 등록일 (to)", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @RequestParam(value = "createdAtTo", required = false) createdAtTo: LocalDateTime?,
        @ParameterObject pageable: Pageable,
    ): NoticeListResponse {
        return noticeManagementUseCase.getNoticeList(
            searchType = searchType,
            searchKeyword = searchKeyword,
            createdAtFrom = createdAtFrom,
            createdAtTo = createdAtTo,
            pageable = pageable,
        ).toResponse()
    }

    @Operation(summary = "공지사항 첨부파일 업로드 API", description = "파일을 업로드하고 업로드된 파일 접근 경로를 제공합니다.")
    @PostMapping(value = ["/attachments"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadNoticeAttachment(
        @RequestPart file: MultipartFile,
    ): UploadNoticeAttachmentResponse = UploadNoticeAttachmentResponse(
        filePath = fileManagementUseCase.uploadNoticeAttachment(file)
    )
}