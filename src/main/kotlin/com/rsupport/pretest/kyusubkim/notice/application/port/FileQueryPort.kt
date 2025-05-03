package com.rsupport.pretest.kyusubkim.notice.application.port

import com.rsupport.pretest.kyusubkim.notice.domain.AttachedFile

interface FileQueryPort {
    fun getFile(fileId: Long): AttachedFile
}