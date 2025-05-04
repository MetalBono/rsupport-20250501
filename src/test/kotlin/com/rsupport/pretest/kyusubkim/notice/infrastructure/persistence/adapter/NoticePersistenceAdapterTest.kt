package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.adapter

import com.rsupport.pretest.kyusubkim.common.config.TestConfig
import com.rsupport.pretest.kyusubkim.notice.application.dto.CreateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.dto.UpdateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeAttachment
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeSearchType
import com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.NoticeAttachmentRepository
import com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.NoticeRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random

@ActiveProfiles("test")
@SpringBootTest
@Import(TestConfig::class)
class NoticePersistenceAdapterTest(
    private val noticeRepository: NoticeRepository,
    private val noticeAttachmentRepository: NoticeAttachmentRepository,
    private val noticePersistenceAdapter: NoticePersistenceAdapter,
) : DescribeSpec({

    afterTest {
        noticeRepository.deleteAll()
        noticeAttachmentRepository.deleteAll()
    }

    describe("공지사항을 신규 등록할 때") {
        context("요청 객체에 적절한 값으로 Dto 를 만들어 호출하면") {
            val attachments = listOf(
                NoticeAttachment(
                    filePath = "file_path_1",
                ),
                NoticeAttachment(
                    filePath = "file_path_2",
                ),
                NoticeAttachment(
                    filePath = "file_path_3",
                ),
            )
            val requestBody = CreateNoticeRequestDto(
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now().plusDays(10),
                createdBy = UUID.randomUUID().toString(),
                attachments = attachments,
            )
            val result = noticePersistenceAdapter.createNotice(requestBody)
            it("새 공지사항을 저장하고 그 정보를 리턴한다.") {
                result shouldNotBe null
                result.title shouldBe requestBody.title
                result.content shouldBe requestBody.content
                result.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe requestBody.startsAt.truncatedTo(ChronoUnit.MILLIS)
                result.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe requestBody.endsAt.truncatedTo(ChronoUnit.MILLIS)
                result.createdBy shouldBe requestBody.createdBy
                result.attachments!!.forEach { att ->
                    attachments.first { att.filePath == it.filePath } shouldNotBe null
                }
            }
        }
    }

    describe("공지사항을 수정할 때") {
        val createdNotice = noticePersistenceAdapter.createNotice(CreateNoticeRequestDto(
            title = UUID.randomUUID().toString(),
            content = UUID.randomUUID().toString(),
            startsAt = LocalDateTime.now(),
            endsAt = LocalDateTime.now().plusDays(10),
            createdBy = UUID.randomUUID().toString(),
            attachments = listOf(
                NoticeAttachment(
                    filePath = "file_path_1",
                ),
                NoticeAttachment(
                    filePath = "file_path_2",
                ),
                NoticeAttachment(
                    filePath = "file_path_3",
                ),
            ),
        ))
        context("요청 객체에 적절한 값으로 Dto 를 만들어 호출하면") {
            val updateTargetFileId = createdNotice.attachments!![1].id!!
            val updateAttachments = listOf(
                NoticeAttachment(
                    filePath = "file_path_1",
                ),
                NoticeAttachment(
                    id = updateTargetFileId,
                    filePath = "file_path_2 modified",
                ),
            )
            val requestBody = UpdateNoticeRequestDto(
                id = createdNotice.id,
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now().plusDays(10),
                updatedBy = UUID.randomUUID().toString(),
                attachments = updateAttachments,
            )
            val result = noticePersistenceAdapter.updateNotice(requestBody)
            it("공지사항을 수정하고, 첨부 파일 추가 / 수정 / 삭제 처리 후 그 정보를 리턴한다.") {
                result shouldNotBe null
                result.title shouldBe requestBody.title
                result.content shouldBe requestBody.content
                result.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe requestBody.startsAt.truncatedTo(ChronoUnit.MILLIS)
                result.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe requestBody.endsAt.truncatedTo(ChronoUnit.MILLIS)
                result.updatedBy shouldBe requestBody.updatedBy
                result.attachments!!.size shouldBe 2

                noticeAttachmentRepository.findAllById(createdNotice.attachments!!.map { it.id }).size shouldBe 1
                noticeAttachmentRepository.findById(updateTargetFileId).get().filePath shouldBe updateAttachments[1].filePath
            }
        }
    }

    describe("공지사항을 삭제할 때") {
        val createdNotice = noticePersistenceAdapter.createNotice(CreateNoticeRequestDto(
            title = UUID.randomUUID().toString(),
            content = UUID.randomUUID().toString(),
            startsAt = LocalDateTime.now(),
            endsAt = LocalDateTime.now().plusDays(10),
            createdBy = UUID.randomUUID().toString(),
            attachments = listOf(
                NoticeAttachment(
                    filePath = "file_path_1",
                ),
                NoticeAttachment(
                    filePath = "file_path_2",
                ),
                NoticeAttachment(
                    filePath = "file_path_3",
                ),
            ),
        ))
        context("존재하는 공지사항 ID 로 삭제를 요청하면") {
            noticePersistenceAdapter.deleteNotice(createdNotice.id)
            it("해당 공지사항과 첨부파일 정보는 삭제된다.") {
                noticeRepository.findById(createdNotice.id).isEmpty shouldBe true
                noticeAttachmentRepository.findAllById(createdNotice.attachments!!.map { it.id!! }).size shouldBe 0
            }
        }
    }

    describe("공지사항의 조회수를 갱신할 때") {
        val createdNotice = noticePersistenceAdapter.createNotice(CreateNoticeRequestDto(
            title = UUID.randomUUID().toString(),
            content = UUID.randomUUID().toString(),
            startsAt = LocalDateTime.now(),
            endsAt = LocalDateTime.now().plusDays(10),
            createdBy = UUID.randomUUID().toString(),
            attachments = listOf(
                NoticeAttachment(
                    filePath = "file_path_1",
                ),
                NoticeAttachment(
                    filePath = "file_path_2",
                ),
                NoticeAttachment(
                    filePath = "file_path_3",
                ),
            ),
        ))
        context("존재하는 공지사항 ID 로 viewCount 와 함께 갱신 요청하면") {
            val viewCount = Random.nextLong(1, 1000)
            noticePersistenceAdapter.increaseNoticeViewCount(createdNotice.id, viewCount)
            it("해당 공지사항의 조회수가 viewCount 만큼 증가한다.") {
                noticeRepository.findById(createdNotice.id).get().viewCount shouldBe createdNotice.viewCount + viewCount
            }
        }
    }

    describe("공지사항 상세를 조회할 때") {
        val createdNotice = noticePersistenceAdapter.createNotice(CreateNoticeRequestDto(
            title = UUID.randomUUID().toString(),
            content = UUID.randomUUID().toString(),
            startsAt = LocalDateTime.now(),
            endsAt = LocalDateTime.now().plusDays(10),
            createdBy = UUID.randomUUID().toString(),
            attachments = listOf(
                NoticeAttachment(
                    filePath = "file_path_1",
                ),
                NoticeAttachment(
                    filePath = "file_path_2",
                ),
                NoticeAttachment(
                    filePath = "file_path_3",
                ),
            ),
        ))
        context("저장된 공지사항의 ID 를 전달하면") {
            val result = noticePersistenceAdapter.getNotice(createdNotice.id)
            it("해당 공지사항이 조회된다.") {
                result shouldNotBe null
                createdNotice.id shouldBe result?.id
                createdNotice.title shouldBe result?.title
                createdNotice.content shouldBe result?.content
                createdNotice.viewCount shouldBe result?.viewCount
                createdNotice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result?.startsAt?.truncatedTo(ChronoUnit.MILLIS)
                createdNotice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result?.endsAt?.truncatedTo(ChronoUnit.MILLIS)
                createdNotice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result?.createdAt?.truncatedTo(ChronoUnit.MILLIS)
                createdNotice.createdBy shouldBe result?.createdBy
                createdNotice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe result?.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                createdNotice.updatedBy shouldBe result?.updatedBy
            }
        }
        context("저장되어있지 않은 ID 를 전달하면") {
            val result = noticePersistenceAdapter.getNotice(Random.nextLong(1001, 2000))
            it("null 이 조회된다.") {
                result shouldBe null
            }
        }
    }

    describe("공지사항 목록을 검색할 때") {
        context("제목 + 내용 검색 조건과 함께 요청을 보내면") {
            (1..5).map {
                noticePersistenceAdapter.createNotice(CreateNoticeRequestDto(
                    title = UUID.randomUUID().toString(),
                    content = "공지사항 콘텐츠${it}의 내용입니다.",
                    startsAt = LocalDateTime.now(),
                    endsAt = LocalDateTime.now().plusDays(10),
                    createdBy = UUID.randomUUID().toString(),
                    attachments = listOf(
                        NoticeAttachment(
                            filePath = "file_path_1",
                        ),
                        NoticeAttachment(
                            filePath = "file_path_2",
                        ),
                        NoticeAttachment(
                            filePath = "file_path_3",
                        ),
                    ),
                ))
            }
            val searchType = NoticeSearchType.TITLE_AND_CONTENT
            val searchKeyword = "콘텐츠2"
            val pageable = PageRequest.of(0, 30)
            val result = noticePersistenceAdapter.getNoticeList(
                searchType = searchType,
                searchKeyword = searchKeyword,
                pageable = pageable,
            )
            it("조건에 맞는 공지사항 목록 정보를 리턴한다.") {
                result.hasNext shouldBe false
                result.totalCount shouldBe 1
                result.totalPages shouldBe 1
                result.list[0].content.contains(searchKeyword) shouldBe true
            }
        }
        context("유효한 등록일 검색 조건과 함께 요청을 보내면") {
            val savedNotices = (1..5).map {
                noticePersistenceAdapter.createNotice(CreateNoticeRequestDto(
                    title = UUID.randomUUID().toString(),
                    content = "공지사항 콘텐츠${it}의 내용입니다.",
                    startsAt = LocalDateTime.now(),
                    endsAt = LocalDateTime.now().plusDays(10),
                    createdBy = UUID.randomUUID().toString(),
                    attachments = listOf(
                        NoticeAttachment(
                            filePath = "file_path_1",
                        ),
                        NoticeAttachment(
                            filePath = "file_path_2",
                        ),
                        NoticeAttachment(
                            filePath = "file_path_3",
                        ),
                    ),
                ))
            }
            val pageSize = 3
            val pageable = PageRequest.of(0, pageSize)
            val result = noticePersistenceAdapter.getNoticeList(
                createdAtFrom = LocalDateTime.now().minusDays(1),
                createdAtTo = LocalDateTime.now().plusDays(1),
                pageable = pageable,
            )
            it("조건에 맞는 공지사항 목록 정보를 리턴한다.") {
                result.hasNext shouldBe true
                result.totalCount shouldBe savedNotices.size
                result.totalPages shouldBe savedNotices.size / pageSize + if (savedNotices.size % pageSize == 0) 0 else 1
            }
        }
        context("유효하지 않은 등록일 검색 조건과 함께 요청을 보내면") {
            (1..5).map {
                noticePersistenceAdapter.createNotice(CreateNoticeRequestDto(
                    title = UUID.randomUUID().toString(),
                    content = "공지사항 콘텐츠${it}의 내용입니다.",
                    startsAt = LocalDateTime.now(),
                    endsAt = LocalDateTime.now().plusDays(10),
                    createdBy = UUID.randomUUID().toString(),
                    attachments = listOf(
                        NoticeAttachment(
                            filePath = "file_path_1",
                        ),
                        NoticeAttachment(
                            filePath = "file_path_2",
                        ),
                        NoticeAttachment(
                            filePath = "file_path_3",
                        ),
                    ),
                ))
            }
            val pageable = PageRequest.of(0, 30)
            val result = noticePersistenceAdapter.getNoticeList(
                createdAtFrom = LocalDateTime.now().minusDays(10),
                createdAtTo = LocalDateTime.now().minusDays(5),
                pageable = pageable,
            )
            it("빈 목록이 리턴된다.") {
                result.hasNext shouldBe false
                result.totalCount shouldBe 0
                result.totalPages shouldBe 0
                result.list.size shouldBe 0
            }
        }
    }

    describe("공지사항 ID 목록으로 공지사항 목록을 조회할 때") {
        val savedNotices = (1..5).map {
            noticePersistenceAdapter.createNotice(CreateNoticeRequestDto(
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now().plusDays(10),
                createdBy = UUID.randomUUID().toString(),
                attachments = listOf(
                    NoticeAttachment(
                        filePath = "file_path_1",
                    ),
                    NoticeAttachment(
                        filePath = "file_path_2",
                    ),
                    NoticeAttachment(
                        filePath = "file_path_3",
                    ),
                ),
            ))
        }
        context("저장된 공지사항의 ID 목록을 전달하면") {
            val result = noticePersistenceAdapter.getNoticeListByIds(savedNotices.map { it.id })
            it("해당 공지사항 목록이 조회된다.") {
                result.size shouldBe savedNotices.size
                result.forEach {
                    val matchingNotice = savedNotices.first { notice -> notice.id == it.id }
                    matchingNotice shouldNotBe null
                    matchingNotice.id shouldBe it.id
                    matchingNotice.title shouldBe it.title
                    matchingNotice.content shouldBe it.content
                    matchingNotice.viewCount shouldBe it.viewCount
                    matchingNotice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe it.startsAt.truncatedTo(ChronoUnit.MILLIS)
                    matchingNotice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe it.endsAt.truncatedTo(ChronoUnit.MILLIS)
                    matchingNotice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe it.createdAt.truncatedTo(ChronoUnit.MILLIS)
                    matchingNotice.createdBy shouldBe it.createdBy
                    matchingNotice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe it.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                    matchingNotice.updatedBy shouldBe it.updatedBy
                }
            }
        }
    }
})