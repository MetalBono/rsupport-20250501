package com.rsupport.pretest.kyusubkim.notice.application.usecase

import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeCacheManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeManagementPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoticeStatisticsUseCase(
    private val noticeManagementPort: NoticeManagementPort,
    private val noticeCacheManagementPort: NoticeCacheManagementPort,
) {
    @Transactional
    fun increaseNoticeViewCount(noticeId: Long, viewCount: Long) {
        noticeManagementPort.increaseNoticeViewCount(noticeId, viewCount)
        noticeCacheManagementPort.increaseNoticeViewCount(noticeId, viewCount)
    }
}