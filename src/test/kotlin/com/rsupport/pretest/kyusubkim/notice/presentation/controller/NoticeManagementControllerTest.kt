package com.rsupport.pretest.kyusubkim.notice.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.rsupport.pretest.kyusubkim.notice.application.dto.CreateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.dto.UpdateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.usecase.FileManagementUseCase
import com.rsupport.pretest.kyusubkim.notice.application.usecase.NoticeManagementUseCase
import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import com.rsupport.pretest.kyusubkim.notice.domain.NoticePageList
import com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto.CreateNoticeRequest
import com.rsupport.pretest.kyusubkim.notice.presentation.controller.dto.UpdateNoticeRequest
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.Matchers
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.*
import org.springframework.web.servlet.function.RequestPredicates.contentType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

@WebMvcTest(NoticeManagementController::class)
class NoticeManagementControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    @MockkBean(relaxed = true) private val fileManagementUseCase: FileManagementUseCase,
    @MockkBean(relaxed = true) private val noticeManagementUseCase: NoticeManagementUseCase,
) : DescribeSpec({
    describe("공지사항을 신규 등록할 때") {
        val noticeId = Random.nextLong()
        context("제목을 보내지 않으면") {
            val result = mockMvc.post("/api/v1/management/notice") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    CreateNoticeRequest(
                        title = null,
                        content = UUID.randomUUID().toString(),
                        startsAt = LocalDateTime.now(),
                        endsAt = LocalDateTime.now(),
                        createdBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("제목은 필수 값 입니다."))
                }
            }
        }
        context("제목을 100자를 초과하여 보내면") {
            val result = mockMvc.post("/api/v1/management/notice") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    CreateNoticeRequest(
                        title = (1..101).map { "A" }.joinToString { "" },
                        content = UUID.randomUUID().toString(),
                        startsAt = LocalDateTime.now(),
                        endsAt = LocalDateTime.now(),
                        createdBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("제목은 1자 ~ 100자 이내로 작성해주세요."))
                }
            }
        }
        context("내용을 보내지 않으면") {
            val result = mockMvc.post("/api/v1/management/notice") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    CreateNoticeRequest(
                        title = UUID.randomUUID().toString(),
                        content = null,
                        startsAt = LocalDateTime.now(),
                        endsAt = LocalDateTime.now(),
                        createdBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("내용은 필수 값 입니다."))
                }
            }
        }
        context("내용을 2000자를 초과하여 보내면") {
            val result = mockMvc.post("/api/v1/management/notice") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    CreateNoticeRequest(
                        title = UUID.randomUUID().toString(),
                        content = (1..2001).map { "A" }.joinToString { "" },
                        startsAt = LocalDateTime.now(),
                        endsAt = LocalDateTime.now(),
                        createdBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("내용은 1자 ~ 2000자 이내로 작성해주세요."))
                }
            }
        }
        context("공지 시작 일시를 보내지 않으면") {
            val result = mockMvc.post("/api/v1/management/notice") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    CreateNoticeRequest(
                        title = UUID.randomUUID().toString(),
                        content = UUID.randomUUID().toString(),
                        startsAt = null,
                        endsAt = LocalDateTime.now(),
                        createdBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("공지 시작 일시 값은 필수 값 입니다."))
                }
            }
        }
        context("공지 종료 일시를 보내지 않으면") {
            val result = mockMvc.post("/api/v1/management/notice") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    CreateNoticeRequest(
                        title = UUID.randomUUID().toString(),
                        content = UUID.randomUUID().toString(),
                        startsAt = LocalDateTime.now(),
                        endsAt = null,
                        createdBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("공지 종료 일시 값은 필수 값 입니다."))
                }
            }
        }
        context("등록자를 보내지 않으면") {
            val result = mockMvc.post("/api/v1/management/notice") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    CreateNoticeRequest(
                        title = UUID.randomUUID().toString(),
                        content = UUID.randomUUID().toString(),
                        startsAt = LocalDateTime.now(),
                        endsAt = LocalDateTime.now(),
                        createdBy = null,
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("등록자는 필수 값 입니다."))
                }
            }
        }
        context("모든 필수 요소들과 함께 정상 등록 요청을 보내면") {
            val requestBody = CreateNoticeRequest(
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now(),
                createdBy = UUID.randomUUID().toString(),
            )
            val createdNotice = Notice(
                id = noticeId,
                title = requestBody.title!!,
                content = requestBody.content!!,
                startsAt = requestBody.startsAt!!,
                endsAt = requestBody.endsAt!!,
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = requestBody.createdBy!!,
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            )
            every { noticeManagementUseCase.createNotice(any(CreateNoticeRequestDto::class)) } returns createdNotice

            val result = mockMvc.post("/api/v1/management/notice") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(requestBody)
            }
            it("새로 등록된 공지사항 정보와 함께 정상 응답을 리턴한다.") {
                result.andExpect {
                    status { isOk() }
                    jsonPath("$.title") { value(requestBody.title) }
                    jsonPath("$.content") { value(requestBody.content) }
                    jsonPath("$.startsAt") { value(requestBody.startsAt!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                    jsonPath("$.endsAt") { value(requestBody.endsAt!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                    jsonPath("$.createdBy") { value(requestBody.createdBy) }
                }
            }
        }
    }

    describe("공지사항을 수정할 때") {
        val noticeId = Random.nextLong()
        context("제목을 보내지 않으면") {
            val result = mockMvc.put("/api/v1/management/notice/${noticeId}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    UpdateNoticeRequest(
                        title = null,
                        content = UUID.randomUUID().toString(),
                        startsAt = LocalDateTime.now(),
                        endsAt = LocalDateTime.now(),
                        updatedBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("제목은 필수 값 입니다."))
                }
            }
        }
        context("제목을 100자를 초과하여 보내면") {
            val result = mockMvc.put("/api/v1/management/notice/${noticeId}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    UpdateNoticeRequest(
                        title = (1..101).map { "A" }.joinToString { "" },
                        content = UUID.randomUUID().toString(),
                        startsAt = LocalDateTime.now(),
                        endsAt = LocalDateTime.now(),
                        updatedBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("제목은 1자 ~ 100자 이내로 작성해주세요."))
                }
            }
        }
        context("내용을 보내지 않으면") {
            val result = mockMvc.put("/api/v1/management/notice/${noticeId}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    UpdateNoticeRequest(
                        title = UUID.randomUUID().toString(),
                        content = null,
                        startsAt = LocalDateTime.now(),
                        endsAt = LocalDateTime.now(),
                        updatedBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("내용은 필수 값 입니다."))
                }
            }
        }
        context("내용을 2000자를 초과하여 보내면") {
            val result = mockMvc.put("/api/v1/management/notice/${noticeId}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    UpdateNoticeRequest(
                        title = UUID.randomUUID().toString(),
                        content = (1..2001).map { "A" }.joinToString { "" },
                        startsAt = LocalDateTime.now(),
                        endsAt = LocalDateTime.now(),
                        updatedBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("내용은 1자 ~ 2000자 이내로 작성해주세요."))
                }
            }
        }
        context("공지 시작 일시를 보내지 않으면") {
            val result = mockMvc.put("/api/v1/management/notice/${noticeId}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    UpdateNoticeRequest(
                        title = UUID.randomUUID().toString(),
                        content = UUID.randomUUID().toString(),
                        startsAt = null,
                        endsAt = LocalDateTime.now(),
                        updatedBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("공지 시작 일시 값은 필수 값 입니다."))
                }
            }
        }
        context("공지 종료 일시를 보내지 않으면") {
            val result = mockMvc.put("/api/v1/management/notice/${noticeId}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    UpdateNoticeRequest(
                        title = UUID.randomUUID().toString(),
                        content = UUID.randomUUID().toString(),
                        startsAt = LocalDateTime.now(),
                        endsAt = null,
                        updatedBy = UUID.randomUUID().toString(),
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("공지 종료 일시 값은 필수 값 입니다."))
                }
            }
        }
        context("수정자를 보내지 않으면") {
            val result = mockMvc.put("/api/v1/management/notice/${noticeId}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    UpdateNoticeRequest(
                        title = UUID.randomUUID().toString(),
                        content = UUID.randomUUID().toString(),
                        startsAt = LocalDateTime.now(),
                        endsAt = LocalDateTime.now(),
                        updatedBy = null,
                    ),
                )
            }
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("수정자는 필수 값 입니다."))
                }
            }
        }
        context("모든 필수 요소들과 함께 정상 수정 요청을 보내면") {
            val requestBody = UpdateNoticeRequest(
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            )
            val updatedNotice = Notice(
                id = noticeId,
                title = requestBody.title!!,
                content = requestBody.content!!,
                startsAt = requestBody.startsAt!!,
                endsAt = requestBody.endsAt!!,
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = UUID.randomUUID().toString(),
                updatedAt = LocalDateTime.now(),
                updatedBy = requestBody.updatedBy!!,
            )
            every { noticeManagementUseCase.updateNotice(any(UpdateNoticeRequestDto::class)) } returns updatedNotice

            val result = mockMvc.put("/api/v1/management/notice/${noticeId}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(requestBody)
            }
            it("수정된 공지사항 정보와 함께 정상 응답을 리턴한다.") {
                result.andExpect {
                    status { isOk() }
                    jsonPath("$.title") { value(requestBody.title) }
                    jsonPath("$.content") { value(requestBody.content) }
                    jsonPath("$.startsAt") { value(requestBody.startsAt!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                    jsonPath("$.endsAt") { value(requestBody.endsAt!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                    jsonPath("$.updatedBy") { value(requestBody.updatedBy) }
                }
            }
        }
    }

    describe("공지사항을 삭제할 때") {
        val noticeId = Random.nextLong()
        context("공지사항 ID 를 이용하여 삭제 요청을 하면") {
            every { noticeManagementUseCase.deleteNotice(noticeId) } returns Unit
            val result = mockMvc.delete("/api/v1/management/notice/${noticeId}")
            it("해당 아이디에 대해 삭제를 실행하고 정상 응답을 리턴한다.") {
                result.andExpect {
                    status { isNoContent() }
                    verify(exactly = 1) { noticeManagementUseCase.deleteNotice(noticeId) }
                }
            }
        }
    }

    describe("공지사항 상세를 조회할 때") {
        val noticeId = Random.nextLong()
        context("공지사항 ID 를 이용하여 조회하면") {
            val mockNotice = Notice(
                id = noticeId,
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now(),
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = UUID.randomUUID().toString(),
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            )
            every { noticeManagementUseCase.getNotice(noticeId) } returns mockNotice
            val result = mockMvc.get("/api/v1/management/notice/${noticeId}")
            it("해당하는 ID의 공지사항을 리턴한다.") {
                result.andExpect {
                    status { isOk() }
                    jsonPath("$.id") { value(mockNotice.id) }
                    jsonPath("$.title") { value(mockNotice.title) }
                    jsonPath("$.content") { value(mockNotice.content) }
                    jsonPath("$.startsAt") { value(mockNotice.startsAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                    jsonPath("$.endsAt") { value(mockNotice.endsAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                    jsonPath("$.viewCount") { value(mockNotice.viewCount) }
                    jsonPath("$.createdAt") { value(mockNotice.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                    jsonPath("$.createdBy") { value(mockNotice.createdBy) }
                    jsonPath("$.updatedAt") { value(mockNotice.updatedAt?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                    jsonPath("$.updatedBy") { value(mockNotice.updatedBy) }
                }
            }
        }
    }

    describe("공지사항 목록을 조회할 때") {
        context("필수 파라미터를 모두 포함하여 정상 요청을 보내면") {
            val pageNumber = Random.nextInt(0, 100)
            val pageSize = Random.nextInt(0, 100)
            val mockNoticePageList = mockk<NoticePageList>(relaxed = true)
            val mockNoticeList = listOf(
                Notice(
                    id = Random.nextLong(),
                    title = UUID.randomUUID().toString(),
                    content = UUID.randomUUID().toString(),
                    startsAt = LocalDateTime.now(),
                    endsAt = LocalDateTime.now(),
                    viewCount = Random.nextLong(),
                    createdAt = LocalDateTime.now(),
                    createdBy = UUID.randomUUID().toString(),
                    updatedAt = LocalDateTime.now(),
                    updatedBy = UUID.randomUUID().toString(),
                )
            )
            every { noticeManagementUseCase.getNoticeList(
                pageable = any(Pageable::class),
            ) } returns mockNoticePageList
            every { mockNoticePageList.hasNext } returns true
            every { mockNoticePageList.totalPages } returns Random.nextInt()
            every { mockNoticePageList.totalCount } returns Random.nextLong()
            every { mockNoticePageList.list } returns mockNoticeList

            val result = mockMvc.get("/api/v1/management/notice/list?pageNumber=${pageNumber}&pageSize=${pageSize}")
            it("정상 응답을 리턴한다.") {
                result.andExpect {
                    status { isOk() }
                    jsonPath("$.hasNext") { value(mockNoticePageList.hasNext) }
                    jsonPath("$.totalPages") { value(mockNoticePageList.totalPages) }
                    jsonPath("$.totalCount") { value(mockNoticePageList.totalCount) }
                    mockNoticeList.forEachIndexed { index, mockNotice ->
                        jsonPath("$.list[${index}].id") { value(mockNotice.id) }
                        jsonPath("$.list[${index}].title") { value(mockNotice.title) }
                        jsonPath("$.list[${index}].content") { value(mockNotice.content) }
                        jsonPath("$.list[${index}].startsAt") { value(mockNotice.startsAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                        jsonPath("$.list[${index}].endsAt") { value(mockNotice.endsAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                        jsonPath("$.list[${index}].viewCount") { value(mockNotice.viewCount) }
                        jsonPath("$.list[${index}].createdAt") { value(mockNotice.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                        jsonPath("$.list[${index}].createdBy") { value(mockNotice.createdBy) }
                        jsonPath("$.list[${index}].updatedAt") { value(mockNotice.updatedAt?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) }
                        jsonPath("$.list[${index}].updatedBy") { value(mockNotice.updatedBy) }
                    }
                }
            }
        }
    }

    describe("공지사항 첨부파일을 업로드할 때") {
        context("multipart 파일을 첨부하면") {
            val mockFile = MockMultipartFile(
                "file",
                "rsupport_test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, world!".toByteArray()
            )
            val mockFilePath = UUID.randomUUID().toString()
            it("파일이 업로드된 경로 정보를 응답으로 리턴한다.") {
                every { fileManagementUseCase.uploadNoticeAttachment(mockFile) } returns mockFilePath
                val result = mockMvc.multipart("/api/v1/management/notice/attachments") {
                    file(mockFile)
                    contentType(MediaType.MULTIPART_FORM_DATA)
                }
                result.andExpect {
                    status { isOk() }
                    jsonPath("$.filePath") { value(mockFilePath) }
                }
            }
        }
    }
})