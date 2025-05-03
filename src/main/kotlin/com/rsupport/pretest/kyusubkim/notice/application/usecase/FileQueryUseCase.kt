package com.rsupport.pretest.kyusubkim.notice.application.usecase

import com.rsupport.pretest.kyusubkim.notice.application.port.FileQueryPort
import com.rsupport.pretest.kyusubkim.notice.domain.AttachedFile
import org.springframework.stereotype.Service

@Service
class FileQueryUseCase(
    private val fileQueryPort: FileQueryPort,
) {
    fun getAttachedFile(fileId: Long): AttachedFile {
        return fileQueryPort.getFile(fileId)
    }
}