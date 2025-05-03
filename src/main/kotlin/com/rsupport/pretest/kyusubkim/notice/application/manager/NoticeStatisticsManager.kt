package com.rsupport.pretest.kyusubkim.notice.application.manager

import com.rsupport.pretest.kyusubkim.notice.application.usecase.NoticeStatisticsUseCase
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class NoticeStatisticsManager(
    private val noticeStatisticsUseCase: NoticeStatisticsUseCase,
) {
    private val viewCountMap = ConcurrentHashMap<Long, AtomicLong>()

    @Async
    fun increaseNoticeViewCount(noticeId: Long) {
        viewCountMap.computeIfAbsent(noticeId) { AtomicLong(0) }.incrementAndGet()
    }

    @Scheduled(fixedRate = 3000)
    fun updateNoticeViewCount() {
        val updateTargets = mutableMapOf<Long, Long>()

        synchronized(viewCountMap) {
            viewCountMap.forEach { (noticeId, viewCount) ->
                val count = viewCount.getAndSet(0)
                if (count > 0) {
                    updateTargets[noticeId] = count
                }
            }
            viewCountMap.clear()
        }

        updateTargets.forEach { (noticeId, count) ->
            try {
                noticeStatisticsUseCase.increaseNoticeViewCount(noticeId, count)
            } catch (e: Exception) {
                viewCountMap.computeIfAbsent(noticeId) { AtomicLong(0) }.addAndGet(count)
            }
        }
    }
}