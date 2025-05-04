package com.rsupport.pretest.kyusubkim.notice.application.usecase

import com.rsupport.pretest.kyusubkim.notice.application.port.FileQueryPort
import com.rsupport.pretest.kyusubkim.notice.domain.AttachedFile
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import kotlin.random.Random

class FileQueryUseCaseTest : DescribeSpec({
    val fileQueryPort = mockk<FileQueryPort>(relaxed = true)
    val fileQueryUseCase = FileQueryUseCase(
        fileQueryPort = fileQueryPort,
    )

    describe("공지사항의 첨부파일을 조회할 때") {
        context("파일의 ID 를 이용하여 조회하면") {
            val fileId = Random.nextLong()
            val mockAttachedFile = AttachedFile(
                id = fileId,
                name = UUID.randomUUID().toString(),
                fileData = "fileData".toByteArray()
            )
            every { fileQueryPort.getFile(fileId) } returns mockAttachedFile

            val result = fileQueryUseCase.getAttachedFile(fileId)
            it("첨부파일 조회 Port 의 getFile 를 호출하고 파일 정보를 리턴한다.") {
                verify(exactly = 1) { fileQueryPort.getFile(fileId) }
                result shouldBe mockAttachedFile
            }
        }
    }
})