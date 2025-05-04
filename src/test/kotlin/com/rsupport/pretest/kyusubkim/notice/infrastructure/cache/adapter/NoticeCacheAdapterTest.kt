package com.rsupport.pretest.kyusubkim.notice.infrastructure.cache.adapter

import com.rsupport.pretest.kyusubkim.common.config.TestConfig
import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.redisson.api.RedissonClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random

@ActiveProfiles("test")
@SpringBootTest
@Import(TestConfig::class)
class NoticeCacheAdapterTest(
    private val redissonClient: RedissonClient,
    private val noticeCacheAdapter: NoticeCacheAdapter,
) : DescribeSpec({

    afterTest {
        redissonClient.keys.flushall()
    }

    describe("공지사항 캐시를 저장할 때") {
        val noticeId = Random.nextLong(1, 1000)
        context("Notice 객체를 전달하면") {
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
            noticeCacheAdapter.setNotice(notice)
            it("해당 ID 를 key 로 하는 새 캐시가 저장된다.") {
                val cached = noticeCacheAdapter.getNotice(noticeId)
                cached shouldNotBe null
                notice.id shouldBe cached?.id
                notice.title shouldBe cached?.title
                notice.content shouldBe cached?.content
                notice.viewCount shouldBe cached?.viewCount
                notice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe cached?.startsAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe cached?.endsAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe cached?.createdAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.createdBy shouldBe cached?.createdBy
                notice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe cached?.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.updatedBy shouldBe cached?.updatedBy
            }
        }
    }

    describe("공지사항 캐시를 삭제할 때") {
        val noticeId = Random.nextLong(1, 1000)
        context("저장된 공지사항의 ID를 전달하면") {
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
            noticeCacheAdapter.setNotice(notice)

            noticeCacheAdapter.deleteNotice(noticeId)
            it("해당 ID 를 key 로 하는 캐시가 삭제된다.") {
                val cached = noticeCacheAdapter.getNotice(noticeId)
                cached shouldBe null
            }
        }
    }

    describe("visibleIds 캐시를 저장할 때") {
        val noticeIds = listOf(
            Random.nextLong(1, 100),
            Random.nextLong(101, 200),
            Random.nextLong(201, 300),
            Random.nextLong(301, 400),
        )
        context("공지사항 ID 목록을 전달하면") {
            noticeCacheAdapter.setVisibleNoticeIds(noticeIds)
            it("visibleIds 가 해당 ID 목록으로 갱신된다.") {
                val cached = noticeCacheAdapter.getVisibleNoticeIds()
                cached.size shouldBe noticeIds.size
                cached.forEach {
                    noticeIds.contains(it) shouldBe true
                }
            }
        }
    }

    describe("공지사항 목록 캐시를 저장할 때") {
        val notices = listOf(
            Notice(
                id = Random.nextLong(1, 100),
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now(),
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = UUID.randomUUID().toString(),
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            ),
            Notice(
                id = Random.nextLong(101, 200),
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now(),
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = UUID.randomUUID().toString(),
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            ),
            Notice(
                id = Random.nextLong(201, 300),
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now(),
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = UUID.randomUUID().toString(),
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            ),
        )
        context("공지사항 목록을 전달하면") {
            noticeCacheAdapter.setNoticeList(notices)
            it("각 공지사항의 ID를 key로 하는 캐시가 저장된다.") {
                val cached = noticeCacheAdapter.getNotices(notices.map { it.id })
                cached.size shouldBe notices.size
                cached.forEach { c ->
                    notices.firstOrNull { it.id == c.id } shouldNotBe null
                }
            }
        }
    }

    describe("visibleIds 캐시에 ID 를 추가할 때") {
        val noticeIds = listOf(
            Random.nextLong(1, 100),
            Random.nextLong(101, 200),
            Random.nextLong(201, 300),
            Random.nextLong(301, 400),
        )
        context("등록되어있지 않은 새 ID를 전달하면") {
            noticeCacheAdapter.setVisibleNoticeIds(noticeIds)

            val newId = Random.nextLong(1001, 2000)
            noticeCacheAdapter.addToVisibleIds(newId)
            it("visibleIds 에 해당 ID가 추가된다.") {
                val cached = noticeCacheAdapter.getVisibleNoticeIds()
                cached.size shouldBe noticeIds.size + 1
                cached.contains(newId) shouldBe true
                noticeIds.forEach {
                    cached.contains(it) shouldBe true
                }
            }
        }
        context("이미 등록된 ID 를 전달하면") {
            noticeCacheAdapter.setVisibleNoticeIds(noticeIds)

            val existingId = noticeIds[0]
            noticeCacheAdapter.addToVisibleIds(existingId)
            it("기존 ID 목록이 그대로 유지된다.") {
                val cached = noticeCacheAdapter.getVisibleNoticeIds()
                cached.size shouldBe noticeIds.size
                cached.forEach {
                    noticeIds.contains(it) shouldBe true
                }
            }
        }
    }

    describe("visibleIds 캐시에서 ID 를 삭제할 때") {
        val noticeIds = listOf(
            Random.nextLong(1, 100),
            Random.nextLong(101, 200),
            Random.nextLong(201, 300),
            Random.nextLong(301, 400),
        )
        context("등록되어있지 않은 ID 를 전달하면") {
            noticeCacheAdapter.setVisibleNoticeIds(noticeIds)

            val newId = Random.nextLong(1001, 2000)
            noticeCacheAdapter.deleteFromVisibleIds(newId)
            it("기존 ID 목록이 그대로 유지된다.") {
                val cached = noticeCacheAdapter.getVisibleNoticeIds()
                cached.size shouldBe noticeIds.size
                cached.forEach {
                    noticeIds.contains(it) shouldBe true
                }
            }
        }
        context("이미 등록된 ID를 전달하면") {
            noticeCacheAdapter.setVisibleNoticeIds(noticeIds)

            val existingId = noticeIds[0]
            noticeCacheAdapter.deleteFromVisibleIds(existingId)
            it("visibleIds 에서 해당 ID가 제거된다.") {
                val cached = noticeCacheAdapter.getVisibleNoticeIds()
                cached.size shouldBe noticeIds.size - 1
                cached.contains(existingId) shouldNotBe true
                cached.forEach {
                    noticeIds.contains(it) shouldBe true
                }
            }
        }
    }

    describe("공지사항의 조회수를 증가시킬 때") {
        val noticeId = Random.nextLong(1, 1000)
        val viewCount = Random.nextLong(1, 100)
        context("저장된 공지사항의 ID를 전달하면") {
            val notice = Notice(
                id = noticeId,
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now(),
                viewCount = Random.nextLong(1, 10),
                createdAt = LocalDateTime.now(),
                createdBy = UUID.randomUUID().toString(),
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            )
            noticeCacheAdapter.setNotice(notice)

            noticeCacheAdapter.increaseNoticeViewCount(
                noticeId = noticeId,
                viewCountIncrement = viewCount,
            )
            it("해당 공지사항의 조회수가 viewCount 만큼 증가된다.") {
                val cached = noticeCacheAdapter.getNotice(noticeId)
                cached?.viewCount shouldBe notice.viewCount + viewCount
            }
        }
    }

    describe("공지사항 캐시를 조회할 때") {
        val noticeId = Random.nextLong(1, 1000)
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
        noticeCacheAdapter.setNotice(notice)
        context("저장된 공지사항의 ID 를 전달하면") {
            val cached = noticeCacheAdapter.getNotice(noticeId)
            it("해당 ID 를 key 로 하는 캐시가 조회된다.") {
                cached shouldNotBe null
                notice.id shouldBe cached?.id
                notice.title shouldBe cached?.title
                notice.content shouldBe cached?.content
                notice.viewCount shouldBe cached?.viewCount
                notice.startsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe cached?.startsAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.endsAt.truncatedTo(ChronoUnit.MILLIS) shouldBe cached?.endsAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe cached?.createdAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.createdBy shouldBe cached?.createdBy
                notice.updatedAt?.truncatedTo(ChronoUnit.MILLIS) shouldBe cached?.updatedAt?.truncatedTo(ChronoUnit.MILLIS)
                notice.updatedBy shouldBe cached?.updatedBy
            }
        }
        context("저장되어있지 않은 ID 를 전달하면") {
            val cached = noticeCacheAdapter.getNotice(Random.nextLong(1001, 2000))
            it("null 이 조회된다.") {
                cached shouldBe null
            }
        }
    }

    describe("visibleIds 캐시를 조회할 때") {
        context("아무것도 등록되어있지 않으면") {
            val cached = noticeCacheAdapter.getVisibleNoticeIds()
            it("빈 목록이 리턴된다.") {
                cached.size shouldBe 0
            }
        }
        context("목록이 등록되어있으면") {
            val noticeIds = listOf(
                Random.nextLong(1, 100),
                Random.nextLong(101, 200),
                Random.nextLong(201, 300),
                Random.nextLong(301, 400),
            )
            noticeCacheAdapter.setVisibleNoticeIds(noticeIds)

            val cached = noticeCacheAdapter.getVisibleNoticeIds()
            it("저장되어있는 목록이 리턴된다.") {
                cached.size shouldBe noticeIds.size
                cached.forEach {
                    noticeIds.contains(it) shouldBe true
                }
            }
        }
    }

    describe("공지사항 목록 캐시를 조회할 때") {
        val notices = listOf(
            Notice(
                id = Random.nextLong(1, 100),
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now(),
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = UUID.randomUUID().toString(),
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            ),
            Notice(
                id = Random.nextLong(101, 200),
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now(),
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = UUID.randomUUID().toString(),
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            ),
            Notice(
                id = Random.nextLong(201, 300),
                title = UUID.randomUUID().toString(),
                content = UUID.randomUUID().toString(),
                startsAt = LocalDateTime.now(),
                endsAt = LocalDateTime.now(),
                viewCount = Random.nextLong(),
                createdAt = LocalDateTime.now(),
                createdBy = UUID.randomUUID().toString(),
                updatedAt = LocalDateTime.now(),
                updatedBy = UUID.randomUUID().toString(),
            ),
        )
        noticeCacheAdapter.setNoticeList(notices)
        context("공지사항 ID 목록을 전달하면") {
            val cached = noticeCacheAdapter.getNotices(notices.map { it.id })
            it("각 공지사항의 ID를 key로 하는 캐시가 조회된다.") {
                cached.size shouldBe notices.size
                cached.forEach { c ->
                    notices.firstOrNull { it.id == c.id } shouldNotBe null
                }
            }
        }
    }
})