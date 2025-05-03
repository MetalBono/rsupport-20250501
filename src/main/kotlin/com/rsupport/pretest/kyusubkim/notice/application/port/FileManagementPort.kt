package com.rsupport.pretest.kyusubkim.notice.application.port

import org.springframework.web.multipart.MultipartFile

interface FileManagementPort {
    fun uploadFile(file: MultipartFile): Long
}