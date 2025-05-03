package com.rsupport.pretest.kyusubkim.notice.application.port

import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import com.rsupport.pretest.kyusubkim.notice.domain.NoticePageList
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeSearchType
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface NoticeQueryPort {
    fun getNotice(noticeId: Long): Notice?

    fun getNoticeList(
        searchType: NoticeSearchType? = null,
        searchKeyword: String? = null,
        createdAtFrom: LocalDateTime? = null,
        createdAtTo: LocalDateTime? = null,
        endsAtFrom: LocalDateTime? = null,
        pageable: Pageable,
    ): NoticePageList

    fun getNoticeListByIds(noticeIds: List<Long>): List<Notice>
}