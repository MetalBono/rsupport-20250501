package com.rsupport.pretest.kyusubkim.notice.application.usecase

import com.rsupport.pretest.kyusubkim.notice.application.port.FileManagementPort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileManagementUseCase(
    private val fileManagementPort: FileManagementPort,
) {
    fun uploadNoticeAttachment(file: MultipartFile): String {
        val fileId = fileManagementPort.uploadFile(file)
        return "https://localhost:8080/api/v1/notice/attachment/${fileId}"
    }
}