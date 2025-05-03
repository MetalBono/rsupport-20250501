package com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto

import com.rsupport.pretest.kyusubkim.notice.application.dto.CreateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.dto.UpdateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeAttachment
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeCursorPageList
import com.rsupport.pretest.kyusubkim.notice.domain.NoticePageList

fun CreateNoticeRequest.toDto() = CreateNoticeRequestDto(
    title = this.title!!,
    content = this.content!!,
    startsAt = this.startsAt!!,
    endsAt = this.endsAt!!,
    attachments = this.attachments?.map { it.toDomain() } ?: emptyList(),
    createdBy = this.createdBy!!,
)

fun CreateNoticeAttachmentRequest.toDomain() = NoticeAttachment(
    filePath = this.filePath!!,
)

fun UpdateNoticeRequest.toDto(noticeId: Long) = UpdateNoticeRequestDto(
    id = noticeId,
    title = this.title!!,
    content = this.content!!,
    startsAt = this.startsAt!!,
    endsAt = this.endsAt!!,
    attachments = this.attachments?.map { it.toDomain() } ?: emptyList(),
    updatedBy = this.updatedBy!!,
)

fun UpdateNoticeAttachmentRequest.toDomain() = NoticeAttachment(
    id = this.id,
    filePath = this.filePath!!,
)

fun Notice.toResponse() = NoticeResponse(
    id = this.id,
    title = this.title,
    content = this.content,
    startsAt = this.startsAt,
    endsAt = this.endsAt,
    viewCount = this.viewCount,
    attachments = this.attachments?.map { it.toResponse() } ?: emptyList(),
    createdAt = this.createdAt,
    createdBy = this.createdBy,
    updatedAt = this.updatedAt,
    updatedBy = this.updatedBy,
)

fun NoticeAttachment.toResponse() = NoticeAttachmentResponse(
    filePath = this.filePath,
)

fun NoticePageList.toResponse() = NoticeListResponse(
    hasNext = this.hasNext,
    totalPages = this.totalPages,
    totalCount = this.totalCount,
    list = this.list.map { it.toResponse() },
)

fun NoticeCursorPageList.toResponse() = NoticeListResponse(
    hasNext = this.hasNext,
    totalPages = this.totalPages,
    totalCount = this.totalCount,
    list = this.list.map { it.toResponse() },
    nextCursor = this.nextCursor,
)