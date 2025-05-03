package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface NoticeRepository : JpaRepository<NoticeEntity, Long>, NoticeRepositoryCustom {
    @Modifying
    @Query("UPDATE NoticeEntity n SET n.viewCount = n.viewCount + :viewCount WHERE n.id = :noticeId")
    fun incrementViewCount(
        @Param("noticeId") noticeId: Long,
        @Param("viewCount") viewCount: Long,
    )
}