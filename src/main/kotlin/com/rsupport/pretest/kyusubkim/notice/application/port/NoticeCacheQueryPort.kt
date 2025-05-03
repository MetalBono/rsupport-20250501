package com.rsupport.pretest.kyusubkim.notice.application.port

import com.rsupport.pretest.kyusubkim.notice.domain.Notice

interface NoticeCacheQueryPort {
    fun getNotice(noticeId: Long): Notice?
    fun getVisibleNoticeIds(): List<Long>
    fun getNotices(noticeIds: List<Long>): List<Notice>
}