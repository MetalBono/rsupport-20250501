package com.rsupport.pretest.kyusubkim.notice.application.usecase

import com.rsupport.pretest.kyusubkim.common.exception.RSupportBadRequestException
import com.rsupport.pretest.kyusubkim.notice.application.manager.NoticeStatisticsManager
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeCacheManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeCacheQueryPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeQueryPort
import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random

class NoticeQueryUseCaseTest : DescribeSpec({
    val noticeCacheQueryPort = mockk<NoticeCacheQueryPort>(relaxed = true)
    val noticeCacheManagementPort = mockk<NoticeCacheManagementPort>(relaxed = true)
    val noticeQueryPort = mockk<NoticeQueryPort>(relaxed = true)
    val noticeStatisticsManager = mockk<NoticeStatisticsManager>(relaxed = true)
    val noticeQueryUseCase = NoticeQueryUseCase(
        noticeCacheQueryPort = noticeCacheQueryPort,
        noticeCacheManagementPort = noticeCacheManagementPort,
        noticeQueryPort = noticeQueryPort,
        noticeStatisticsManager = noticeStatisticsManager,
    )

    describe("공지사항 상세를 조회할 때") {
        context("존재하지 않는 공지사항 ID 를 이용하여 조회하면") {
            val noticeId = Random.nextLong(1, 100)
            every { noticeCacheQueryPort.getNotice(noticeId) } returns null
            every { noticeQueryPort.getNotice(noticeId) } returns null
            val exception = shouldThrow<RSupportBadRequestException> {
                noticeQueryUseCase.getNotice(noticeId)
            }
            it("오류가 발생한다.") {
                exception.message shouldBe "존재하지 않는 공지사항입니다. id - $noticeId"
            }
        }
        context("캐싱되어있는 공지사항 ID 를 이용하여 조회하면") {
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
            every { noticeCacheQueryPort.getNotice(noticeId) } returns notice
            val result = noticeQueryUseCase.getNotice(noticeId)
            it("캐시만을 이용해서 해당 ID의 공지사항을 리턴한다. 이때 조회수를 1 증가시켜서 리턴한다.") {
                verify(exactly = 1) { noticeCacheQueryPort.getNotice(noticeId) }
                verify(exactly = 0) { noticeQueryPort.getNotice(noticeId) }
                verify(exactly = 0) { noticeCacheManagementPort.setNotice(any()) }
                verify(exactly = 1) { noticeStatisticsManager.increaseNoticeViewCount(noticeId) }
                notice.id shouldBe result.id
                notice.title shouldBe result.title
                notice.content shouldBe result.content
                notice.viewCount + 1 shouldBe result.viewCount
                notice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.startsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.endsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.createdAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdBy shouldBe result.createdBy
                notice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe result.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.updatedBy shouldBe result.updatedBy
            }
        }
        context("캐싱되어있지 않은 공지사항 ID 를 이용하여 조회하면") {
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
            every { noticeCacheQueryPort.getNotice(noticeId) } returns null
            every { noticeQueryPort.getNotice(noticeId) } returns notice
            val result = noticeQueryUseCase.getNotice(noticeId)
            it("DB 에서 해당하는 ID의 공지사항을 조회하고, 캐싱한 후에 해당 공지사항을 리턴한다. 이때 조회수를 1 증가시켜서 리턴한다.") {
                verify(exactly = 1) { noticeCacheQueryPort.getNotice(noticeId) }
                verify(exactly = 1) { noticeQueryPort.getNotice(noticeId) }
                verify(exactly = 1) { noticeCacheManagementPort.setNotice(notice) }
                verify(exactly = 1) { noticeStatisticsManager.increaseNoticeViewCount(noticeId) }
                notice.id shouldBe result.id
                notice.title shouldBe result.title
                notice.content shouldBe result.content
                notice.viewCount + 1 shouldBe result.viewCount
                notice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.startsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.endsAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe result.createdAt.truncatedTo(ChronoUnit.MILLIS)
                notice.createdBy shouldBe result.createdBy
                notice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe result.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.updatedBy shouldBe result.updatedBy
            }
        }
    }
})