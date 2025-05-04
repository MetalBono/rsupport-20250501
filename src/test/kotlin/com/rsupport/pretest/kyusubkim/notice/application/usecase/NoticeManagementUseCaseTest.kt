package com.rsupport.pretest.kyusubkim.notice.application.usecase

import com.rsupport.pretest.kyusubkim.common.exception.RSupportBadRequestException
import com.rsupport.pretest.kyusubkim.notice.application.dto.CreateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.dto.UpdateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeCacheManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeQueryPort
import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeSearchType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random

class NoticeManagementUseCaseTest : DescribeSpec({
    val noticeCacheManagementPort = mockk<NoticeCacheManagementPort>(relaxed = true)
    val noticeManagementPort = mockk<NoticeManagementPort>(relaxed = true)
    val noticeQueryPort = mockk<NoticeQueryPort>(relaxed = true)
    val noticeManagementUseCase = NoticeManagementUseCase(
        noticeCacheManagementPort = noticeCacheManagementPort,
        noticeManagementPort = noticeManagementPort,
        noticeQueryPort = noticeQueryPort,
    )

    describe("공지사항을 신규 등록할 때") {
        val noticeId = Random.nextLong(1, 100)
        val requestBody = CreateNoticeRequestDto(
            title = UUID.randomUUID().toString(),
            content = UUID.randomUUID().toString(),
            startsAt = LocalDateTime.now(),
            endsAt = LocalDateTime.now().plusDays(10),
            createdBy = UUID.randomUUID().toString(),
        )
        context("현재 진행중이거나 미래에 진행될 예정인 경우") {
            val notice = Notice(
                id = noticeId,
                title = requestBody.title,
                content = requestBody.content,
                startsAt = requestBody.startsAt,
                endsAt = requestBody.endsAt,
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = requestBody.createdBy,
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            )
            every { noticeManagementPort.createNotice(requestBody) } returns notice
            val result = noticeManagementUseCase.createNotice(requestBody)
            it("visibleIds 캐시에 해당 공지사항의 ID를 추가한 후, 새로 생성된 공지사항 정보를 리턴한다.") {
                verify(exactly = 1) { noticeManagementPort.createNotice(requestBody) }
                verify(exactly = 1) { noticeCacheManagementPort.setNotice(notice) }
                verify(exactly = 1) { noticeCacheManagementPort.addToVisibleIds(noticeId) }
                notice.id shouldBe result.id
                notice.title shouldBe result.title
                notice.content shouldBe result.content
                notice.viewCount shouldBe result.viewCount
                notice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.startsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.endsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.createdAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdBy shouldBe result.createdBy
                notice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe result.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.updatedBy shouldBe result.updatedBy
            }
        }
        context("종료된 공지사항의 경우") {
            val noticeId = Random.nextLong(1, 100)
            val requestBody = CreateNoticeRequestDto(
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now().minusDays(10),
                endsAt = LocalDateTime.now().minusDays(5),
                createdBy = UUID.randomUUID().toString(),
            )
            val notice = Notice(
                id = noticeId,
                title = requestBody.title,
                content = requestBody.content,
                startsAt = requestBody.startsAt,
                endsAt = requestBody.endsAt,
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = requestBody.createdBy,
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            )
            every { noticeManagementPort.createNotice(requestBody) } returns notice
            val result = noticeManagementUseCase.createNotice(requestBody)
            it("visibleIds 캐시에 해당 공지사항의 ID를 추가하지 않고, 공지사항만 생성하여 리턴한다.") {
                verify(exactly = 1) { noticeManagementPort.createNotice(requestBody) }
                verify(exactly = 1) { noticeCacheManagementPort.setNotice(notice) }
                verify(exactly = 0) { noticeCacheManagementPort.addToVisibleIds(noticeId) }
                notice.id shouldBe result.id
                notice.title shouldBe result.title
                notice.content shouldBe result.content
                notice.viewCount shouldBe result.viewCount
                notice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.startsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.endsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.createdAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdBy shouldBe result.createdBy
                notice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe result.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.updatedBy shouldBe result.updatedBy
            }
        }
    }

    describe("공지사항을 수정할 때") {
        context("현재 진행중이거나 미래에 진행될 예정인 경우") {
            val noticeId = Random.nextLong(1, 100)
            val requestBody = UpdateNoticeRequestDto(
                id = noticeId,
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now().plusDays(10),
                updatedBy = UUID.randomUUID().toString(),
            )
            val notice = Notice(
                id = noticeId,
                title = requestBody.title,
                content = requestBody.content,
                startsAt = requestBody.startsAt,
                endsAt = requestBody.endsAt,
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = requestBody.updatedBy,
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            )
            every { noticeManagementPort.updateNotice(requestBody) } returns notice
            val result = noticeManagementUseCase.updateNotice(requestBody)
            it("visibleIds 캐시에 해당 공지사항의 ID를 추가한 후, 수정된 공지사항 정보를 리턴한다.") {
                verify(exactly = 1) { noticeManagementPort.updateNotice(requestBody) }
                verify(exactly = 1) { noticeCacheManagementPort.setNotice(notice) }
                verify(exactly = 1) { noticeCacheManagementPort.addToVisibleIds(noticeId) }
                verify(exactly = 0) { noticeCacheManagementPort.deleteFromVisibleIds(noticeId) }
                notice.id shouldBe result.id
                notice.title shouldBe result.title
                notice.content shouldBe result.content
                notice.viewCount shouldBe result.viewCount
                notice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.startsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.endsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.createdAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdBy shouldBe result.createdBy
                notice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe result.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.updatedBy shouldBe result.updatedBy
            }
        }
        context("종료된 공지사항의 경우") {
            val noticeId = Random.nextLong(1, 100)
            val requestBody = UpdateNoticeRequestDto(
                id = noticeId,
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now().minusDays(10),
                endsAt = LocalDateTime.now().minusDays(5),
                updatedBy = UUID.randomUUID().toString(),
            )
            val notice = Notice(
                id = noticeId,
                title = requestBody.title,
                content = requestBody.content,
                startsAt = requestBody.startsAt,
                endsAt = requestBody.endsAt,
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = requestBody.updatedBy,
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            )
            every { noticeManagementPort.updateNotice(requestBody) } returns notice
            val result = noticeManagementUseCase.updateNotice(requestBody)
            it("visibleIds 캐시에서 해당 공지사항의 ID를 삭제하고, 수정된 공지사항 정보를 리턴한다.") {
                verify(exactly = 1) { noticeManagementPort.updateNotice(requestBody) }
                verify(exactly = 1) { noticeCacheManagementPort.setNotice(notice) }
                verify(exactly = 0) { noticeCacheManagementPort.addToVisibleIds(noticeId) }
                verify(exactly = 1) { noticeCacheManagementPort.deleteFromVisibleIds(noticeId) }
                notice.id shouldBe result.id
                notice.title shouldBe result.title
                notice.content shouldBe result.content
                notice.viewCount shouldBe result.viewCount
                notice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.startsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.endsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.createdAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdBy shouldBe result.createdBy
                notice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe result.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.updatedBy shouldBe result.updatedBy
            }
        }
    }

    describe("공지사항을 삭제할 때") {
        context("공지사항 ID 를 이용하여 삭제 요청하면") {
            val noticeId = Random.nextLong(1, 100)
            noticeManagementUseCase.deleteNotice(noticeId)
            it("DB, 캐시, visibleIds 캐시에 삭제 요청 후 종료된다.") {
                verify(exactly = 1) { noticeManagementPort.deleteNotice(noticeId) }
                verify(exactly = 1) { noticeCacheManagementPort.deleteNotice(noticeId) }
                verify(exactly = 1) { noticeCacheManagementPort.deleteFromVisibleIds(noticeId) }
            }
        }
    }

    describe("공지사항 상세를 조회할 때") {
        context("존재하지 않는 공지사항 ID 를 이용하여 조회하면") {
            val noticeId = Random.nextLong(1, 100)
            every { noticeQueryPort.getNotice(noticeId) } returns null
            val exception = shouldThrow<RSupportBadRequestException> {
                noticeManagementUseCase.getNotice(noticeId)
            }
            it("오류가 발생한다.") {
                exception.message shouldBe "존재하지 않는 공지사항입니다. id - $noticeId"
            }
        }
        context("유효한 공지사항 ID 를 이용하여 조회하면") {
            val noticeId = Random.nextLong(1, 100)
            val notice = Notice(
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
            every { noticeQueryPort.getNotice(noticeId) } returns notice
            val result = noticeManagementUseCase.getNotice(noticeId)
            it("해당하는 ID의 공지사항을 리턴한다.") {
                verify(exactly = 1) { noticeQueryPort.getNotice(noticeId) }
                notice.id shouldBe result.id
                notice.title shouldBe result.title
                notice.content shouldBe result.content
                notice.viewCount shouldBe result.viewCount
                notice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.startsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.endsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.createdAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdBy shouldBe result.createdBy
                notice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe result.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.updatedBy shouldBe result.updatedBy
            }
        }
    }

    describe("공지사항 목록을 검색할 때") {
        context("검색 조건과 함께 요청을 보내면") {
            val searchType = NoticeSearchType.TITLE
            val searchKeyword = UUID.randomUUID().toString()
            val createdAtFrom = LocalDateTime.now().minusDays(1)
            val createdAtTo = LocalDateTime.now().plusDays(1)
            val pageable = PageRequest.of(0, 30)
            every { noticeQueryPort.getNoticeList(
                searchType = searchType,
                searchKeyword = searchKeyword,
                createdAtFrom = createdAtFrom,
                createdAtTo = createdAtTo,
                pageable = any(),
            ) } returns mockk()
            noticeManagementUseCase.getNoticeList(
                searchType = searchType,
                searchKeyword = searchKeyword,
                createdAtFrom = createdAtFrom,
                createdAtTo = createdAtTo,
                pageable = pageable,
            )
            it("공지사항 조회 Port 의 getNoticeList 를 호출하고 조건에 맞는 공지사항 목록 정보를 리턴한다.") {
                verify(exactly = 1) { noticeQueryPort.getNoticeList(
                    searchType = searchType,
                    searchKeyword = searchKeyword,
                    createdAtFrom = createdAtFrom,
                    createdAtTo = createdAtTo,
                    pageable = any(),
                ) }
            }
        }
    }
})