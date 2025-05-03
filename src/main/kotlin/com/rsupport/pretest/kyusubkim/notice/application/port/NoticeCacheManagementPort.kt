package com.rsupport.pretest.kyusubkim.notice.application.port

import com.rsupport.pretest.kyusubkim.notice.domain.Notice

interface NoticeCacheManagementPort {
    fun setNotice(notice: Notice)
    fun deleteNotice(noticeId: Long)
    fun setVisibleNoticeIds(noticeIds: List<Long>): List<Long>
    fun setNoticeList(notices: List<Notice>)
    fun addToVisibleIds(noticeId: Long)
    fun deleteFromVisibleIds(noticeId: Long)
    fun increaseNoticeViewCount(noticeId: Long, viewCountIncrement: Long)
}