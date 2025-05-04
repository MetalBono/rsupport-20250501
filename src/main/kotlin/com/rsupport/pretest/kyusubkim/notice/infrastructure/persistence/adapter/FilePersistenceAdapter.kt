package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.adapter

import com.rsupport.pretest.kyusubkim.common.exception.RSupportBadRequestException
import com.rsupport.pretest.kyusubkim.notice.application.port.FileManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.FileQueryPort
import com.rsupport.pretest.kyusubkim.notice.domain.AttachedFile
import com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.AttachedFilesEntity
import com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.AttachedFilesRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Repository
class FilePersistenceAdapter(
    private val attachedFilesRepository: AttachedFilesRepository,
) : FileManagementPort, FileQueryPort {
    @Transactional
    override fun uploadFile(file: MultipartFile): Long {
        val fileData = file.bytes
        val fileName = file.originalFilename ?: "notice_attachment_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 6)}"

        // 파일 데이터를 DB에 저장
        val fileEntity = AttachedFilesEntity(
            name = fileName,
            fileData = fileData
        )
        return attachedFilesRepository.save(fileEntity).id!!
    }

    @Transactional(readOnly = true)
    override fun getFile(fileId: Long): AttachedFile {
        return attachedFilesRepository.findById(fileId)
            .map { it.toDomain() }
            .orElseThrow {
                throw RSupportBadRequestException("존재하지 않는 파일입니다. id - $fileId")
            }
    }
}