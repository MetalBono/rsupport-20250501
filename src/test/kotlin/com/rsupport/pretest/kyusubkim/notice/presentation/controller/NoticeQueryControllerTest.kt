package com.rsupport.pretest.kyusubkim.notice.presentation.controller

import com.ninjasquad.springmockk.MockkBean
import com.rsupport.pretest.kyusubkim.notice.application.usecase.FileQueryUseCase
import com.rsupport.pretest.kyusubkim.notice.application.usecase.NoticeQueryUseCase
import com.rsupport.pretest.kyusubkim.notice.domain.AttachedFile
import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeCursorPageList
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Matchers
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

@WebMvcTest(NoticeQueryController::class)
class NoticeQueryControllerTest(
    private val mockMvc: MockMvc,
    @MockkBean(relaxed = true) private val fileQueryUseCase: FileQueryUseCase,
    @MockkBean(relaxed = true) private val noticeQueryUseCase: NoticeQueryUseCase,
) : DescribeSpec({
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
            every { noticeQueryUseCase.getNotice(noticeId) } returns mockNotice
            val result = mockMvc.get("/api/v1/notice/${noticeId}")
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
        context("페이지 크기를 보내지 않으면") {
            val result = mockMvc.get("/api/v1/notice/list")
            it("오류가 발생한다.") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.message", Matchers.`is`("필수 요청 파라미터 'pageSize'이(가) 누락되었습니다."))
                }
            }
        }
        context("필수 파라미터를 모두 포함하여 정상 요청을 보내면") {
            val pageSize = Random.nextInt(0, 100)
            val mockNoticeCursorPageList = mockk<NoticeCursorPageList>(relaxed = true)
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
            every { noticeQueryUseCase.getNoticeList(
                pageSize = pageSize,
            ) } returns mockNoticeCursorPageList
            every { mockNoticeCursorPageList.hasNext } returns true
            every { mockNoticeCursorPageList.totalPages } returns Random.nextInt()
            every { mockNoticeCursorPageList.totalCount } returns Random.nextLong()
            every { mockNoticeCursorPageList.list } returns mockNoticeList
            every { mockNoticeCursorPageList.nextCursor } returns Random.nextLong()

            val result = mockMvc.get("/api/v1/notice/list?pageSize=$pageSize")
            it("정상 응답을 리턴한다.") {
                result.andExpect {
                    status { isOk() }
                    jsonPath("$.hasNext") { value(mockNoticeCursorPageList.hasNext) }
                    jsonPath("$.totalPages") { value(mockNoticeCursorPageList.totalPages) }
                    jsonPath("$.totalCount") { value(mockNoticeCursorPageList.totalCount) }
                    jsonPath("$.nextCursor") { value(mockNoticeCursorPageList.nextCursor) }
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

    describe("공지사항 첨부파일을 다운로드 할 때") {
        val fileId = Random.nextLong()
        val fileName = "rsupport_test.txt"
        val fileContent = "hello world".toByteArray()
        context("첨부파일 ID 를 이용하여 조회하면") {
            val mockAttachedFile = AttachedFile(
                id = fileId,
                name = fileName,
                fileData = fileContent,
            )
            every { fileQueryUseCase.getAttachedFile(fileId) } returns mockAttachedFile

            val result = mockMvc.get("/api/v1/notice/attachment/${fileId}")
            it("해당하는 ID의 파일을 리턴한다.") {
                result.andExpect {
                    status { isOk() }
                    content { bytes(fileContent) }
                }
            }
        }
    }
})