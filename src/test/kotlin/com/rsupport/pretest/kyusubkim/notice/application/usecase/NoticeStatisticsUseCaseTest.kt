package com.rsupport.pretest.kyusubkim.notice.application.usecase

import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeCacheManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeManagementPort
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.mockk
import io.mockk.verify
import kotlin.random.Random

class NoticeStatisticsUseCaseTest : DescribeSpec({
    val noticeManagementPort = mockk<NoticeManagementPort>(relaxed = true)
    val noticeCacheManagementPort = mockk<NoticeCacheManagementPort>(relaxed = true)
    val noticeStatisticsUseCase = NoticeStatisticsUseCase(
        noticeManagementPort = noticeManagementPort,
        noticeCacheManagementPort = noticeCacheManagementPort,
    )

    describe("공지사항의 조회수를 집계할 때") {
        context("공지사항 ID 로 조회 수 를 업데이트 요청하면") {
            val noticeId = Random.nextLong(0, 100)
            val viewCount = Random.nextLong(0, 100)

            noticeStatisticsUseCase.increaseNoticeViewCount(noticeId, viewCount)
            it("DB 와 Redis 에 해당 ID 의 공지사항 조회 수로 업데이트를 요청한다.") {
                verify(exactly = 1) { noticeManagementPort.increaseNoticeViewCount(noticeId, viewCount) }
                verify(exactly = 1) { noticeCacheManagementPort.increaseNoticeViewCount(noticeId, viewCount) }
            }
        }
    }
})