package com.rsupport.pretest.kyusubkim.notice.application.dto

import com.rsupport.pretest.kyusubkim.common.exception.RSupportBadRequestException
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeAttachment
import java.time.LocalDateTime

data class CreateNoticeRequestDto(
    val title: String,
    val content: String,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val attachments: List<NoticeAttachment>?,
    val createdBy: String,
) {
    init {
        if (startsAt.isAfter(endsAt)) {
            throw RSupportBadRequestException("공지 시작일은 종료일보다 빨라야 합니다.")
        }
    }
}