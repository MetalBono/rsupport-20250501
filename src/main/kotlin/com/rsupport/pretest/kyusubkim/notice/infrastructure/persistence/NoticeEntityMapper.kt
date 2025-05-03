package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence

import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeAttachment

fun NoticeEntity.toDomain() = Notice(
    id = this.id!!,
    title = this.title,
    content = this.content,
    startsAt = this.startsAt,
    endsAt = this.endsAt,
    viewCount = this.viewCount,
    attachments = this.attachments?.map { it.toDomain() }?.toList(),
    createdAt = this.createdAt,
    createdBy = this.createdBy,
    updatedAt = this.updatedAt,
    updatedBy = this.updatedBy,
)

fun NoticeAttachment.toEntity() = NoticeAttachmentEntity(
    id = this.id,
    filePath = this.filePath,
)

fun NoticeAttachmentEntity.toDomain() = NoticeAttachment(
    id = this.id,
    filePath = this.filePath,
)