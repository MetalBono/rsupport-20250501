package com.rsupport.pretest.kyusubkim.notice.application.usecase

import com.rsupport.pretest.kyusubkim.notice.application.port.FileManagementPort
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldEndWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.multipart.MultipartFile
import kotlin.random.Random

class FileManagementUseCaseTest : DescribeSpec({
    val fileManagementPort = mockk<FileManagementPort>(relaxed = true)
    val fileManagementUseCase = FileManagementUseCase(
        fileManagementPort = fileManagementPort,
    )

    describe("공지사항의 첨부파일을 업로드할 때") {
        context("Multipart File 을 전달하면") {
            val fileId = Random.nextLong()
            val mockFile = mockk<MultipartFile>(relaxed = true)
            every { fileManagementPort.uploadFile(mockFile) } returns fileId

            val result = fileManagementUseCase.uploadNoticeAttachment(mockFile)
            it("첨부파일 관리 Port 의 uploadFile 를 호출하고 업로드된 파일의 접근 경로 문자열을 리턴한다.") {
                verify(exactly = 1) { fileManagementPort.uploadFile(mockFile) }
                result shouldEndWith "/api/v1/notice/attachment/${fileId}"
            }
        }
    }
})