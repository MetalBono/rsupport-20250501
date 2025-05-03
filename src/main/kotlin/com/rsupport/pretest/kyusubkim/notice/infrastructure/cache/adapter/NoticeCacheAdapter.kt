package com.rsupport.pretest.kyusubkim.notice.infrastructure.cache.adapter

import com.rsupport.pretest.kyusubkim.common.exception.RSupportException
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeCacheManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeCacheQueryPort
import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import org.redisson.api.RBucket
import org.redisson.api.RFuture
import org.redisson.api.RSet
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.concurrent.TimeUnit

@Repository
class NoticeCacheAdapter(
    private val redissonClient: RedissonClient,
) : NoticeCacheQueryPort, NoticeCacheManagementPort {
    override fun setNotice(notice: Notice) {
        val bucket: RBucket<Notice> = redissonClient.getBucket("notice:${notice.id}")
        bucket.set(notice, Duration.ofSeconds(NOTICE_CACHE_TTL_SECONDS))
    }

    override fun deleteNotice(noticeId: Long) {
        val bucket: RBucket<Notice> = redissonClient.getBucket("notice:${noticeId}")
        bucket.delete()
    }

    override fun setVisibleNoticeIds(noticeIds: List<Long>): List<Long> {
        val rset: RSet<Long> = redissonClient.getSet("notice:visible_ids")
        rset.clear()
        rset.addAll(noticeIds)
        rset.expire(Duration.ofSeconds(VISIBLE_NOTICE_IDS_CACHE_TTL_SECONDS))
        return noticeIds
    }

    override fun setNoticeList(notices: List<Notice>) {
        val batch = redissonClient.createBatch()
        notices.forEach {
            val bucket = batch.getBucket<Notice>("notice:${it.id}")
            bucket.setAsync(it, Duration.ofSeconds(NOTICE_CACHE_TTL_SECONDS))
        }
        batch.execute()
    }

    override fun addToVisibleIds(noticeId: Long) {
        val rset: RSet<Long> = redissonClient.getSet("notice:visible_ids")
        rset.add(noticeId)
    }

    override fun deleteFromVisibleIds(noticeId: Long) {
        val rset: RSet<Long> = redissonClient.getSet("notice:visible_ids")
        rset.remove(noticeId)
    }

    override fun increaseNoticeViewCount(noticeId: Long, viewCountIncrement: Long) {
        val lockKey = "lock:notice:$noticeId"
        val lock = redissonClient.getLock(lockKey)
        try {
            if (lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                val notice = getNotice(noticeId)
                if (notice != null) {
                    setNotice(notice.copy(
                        viewCount = notice.viewCount + viewCountIncrement
                    ))
                }
            } else {
                throw RSupportException("Redis lock 획득 실패. noticeId - $noticeId")
            }
        } catch (e: Exception) {
            throw RSupportException("공지사항 id - $noticeId 조회 수 처리 중 오류 발생. message - ${e.message}")
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }

    override fun getNotice(noticeId: Long): Notice? {
        val bucket: RBucket<Notice> = redissonClient.getBucket("notice:${noticeId}")
        return bucket.get()
    }

    override fun getVisibleNoticeIds(): List<Long> {
        val rset: RSet<Long> = redissonClient.getSet("notice:visible_ids")
        return rset.toList()
    }

    override fun getNotices(noticeIds: List<Long>): List<Notice> {
        val batch = redissonClient.createBatch()
        val futures: Map<Long, RFuture<Notice>> = noticeIds.associateWith { id ->
            batch.getBucket<Notice>("notice:${id}").async
        }
        batch.execute()
        return noticeIds
            .mapNotNull { id -> futures[id]?.get() }
    }

    companion object {
        const val NOTICE_CACHE_TTL_SECONDS = 3600L
        const val VISIBLE_NOTICE_IDS_CACHE_TTL_SECONDS = 600L
    }
}