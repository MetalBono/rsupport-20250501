package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface AttachedFilesRepository : JpaRepository<AttachedFilesEntity, Long> {
}