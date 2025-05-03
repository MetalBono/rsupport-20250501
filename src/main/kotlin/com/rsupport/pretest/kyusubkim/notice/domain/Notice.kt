package com.rsupport.pretest.kyusubkim.notice.domain

import com.rsupport.pretest.kyusubkim.common.exception.RSupportBadRequestException
import java.time.LocalDateTime

data class Notice(
    val id: Long,
    val title: String,
    val content: String,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val viewCount: Long,
    val attachments: List<NoticeAttachment>? = null,
    val createdAt: LocalDateTime,
    val createdBy: String,
    val updatedAt: LocalDateTime? = null,
    val updatedBy: String? = null,
) {
    init {
        if (startsAt.isAfter(endsAt)) {
            throw RSupportBadRequestException("공지 시작일은 종료일보다 빨라야 합니다.")
        }
    }

    fun isVisibleAt(dateTime: LocalDateTime): Boolean {
        return (this.startsAt.isEqual(dateTime) || this.startsAt.isBefore(dateTime))
                && (this.endsAt.isEqual(dateTime) || this.endsAt.isAfter(dateTime))
    }
}
