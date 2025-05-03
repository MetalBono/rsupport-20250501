package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface NoticeAttachmentRepository : JpaRepository<NoticeAttachmentEntity, Long> {
}