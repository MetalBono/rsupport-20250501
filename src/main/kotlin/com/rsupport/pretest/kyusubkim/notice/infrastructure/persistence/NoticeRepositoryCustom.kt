package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence

import com.rsupport.pretest.kyusubkim.notice.domain.NoticeSearchType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface NoticeRepositoryCustom {
    fun findAllBy(
        searchType: NoticeSearchType? = null,
        searchKeyword: String? = null,
        createdAtFrom: LocalDateTime? = null,
        createdAtTo: LocalDateTime? = null,
        endsAtFrom: LocalDateTime? = null,
        endsAtTo: LocalDateTime? = null,
        pageable: Pageable,
    ): Page<NoticeEntity>
}