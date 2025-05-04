package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.adapter

import com.rsupport.pretest.kyusubkim.common.config.TestConfig
import com.rsupport.pretest.kyusubkim.common.exception.RSupportBadRequestException
import com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.AttachedFilesRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import java.util.*
import kotlin.random.Random

@ActiveProfiles("test")
@SpringBootTest
@Import(TestConfig::class)
class FilePersistenceAdapterTest(
    private val attachedFilesRepository: AttachedFilesRepository,
    private val filePersistenceAdapter: FilePersistenceAdapter,
) : DescribeSpec({

    afterTest {
        attachedFilesRepository.deleteAll()
    }

    describe("첨부파일을 저장할 때") {
        val fileName = UUID.randomUUID().toString() + ".txt"
        context("MultipartFile 을 전달하면") {
            val file = MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, world!".toByteArray(),
            )
            val fileId = filePersistenceAdapter.uploadFile(file)
            it("파일이 저장되고 해당 파일의 id 를 리턴한다.") {
                val uploadedFile = filePersistenceAdapter.getFile(fileId)
                uploadedFile shouldNotBe null
                file.originalFilename shouldBe uploadedFile.name
            }
        }
    }

    describe("첨부파일을 조회할 때") {
        context("존재하지 않는 파일 ID 를 전달하면") {
            val fileId = Random.nextLong(1000, 2000)
            val exception = shouldThrow<RSupportBadRequestException> {
                filePersistenceAdapter.getFile(fileId)
            }
            it("오류가 발생한다.") {
                exception.message shouldBe "존재하지 않는 파일입니다. id - $fileId"
            }
        }
        context("존재하는 파일 ID 를 전달하면") {
            val fileName = UUID.randomUUID().toString() + ".txt"
            val file = MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, world!".toByteArray(),
            )
            val fileId = filePersistenceAdapter.uploadFile(file)

            val uploadedFile = filePersistenceAdapter.getFile(fileId)
            it("파일이 저장되고 해당 파일의 id 를 리턴한다.") {
                uploadedFile shouldNotBe null
                file.originalFilename shouldBe uploadedFile.name
            }
        }
    }
})