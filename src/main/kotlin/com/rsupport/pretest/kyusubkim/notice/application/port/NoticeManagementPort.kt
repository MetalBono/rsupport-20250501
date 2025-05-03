package com.rsupport.pretest.kyusubkim.notice.application.port

import com.rsupport.pretest.kyusubkim.notice.application.dto.CreateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.dto.UpdateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.domain.Notice

interface NoticeManagementPort {
    fun createNotice(dto: CreateNoticeRequestDto): Notice
    fun updateNotice(dto: UpdateNoticeRequestDto): Notice
    fun deleteNotice(noticeId: Long)
    fun increaseNoticeViewCount(noticeId: Long, viewCount: Long)
}