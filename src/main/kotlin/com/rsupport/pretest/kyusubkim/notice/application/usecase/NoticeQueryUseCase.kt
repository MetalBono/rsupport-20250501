package com.rsupport.pretest.kyusubkim.notice.application.usecase

import com.rsupport.pretest.kyusubkim.common.exception.RSupportBadRequestException
import com.rsupport.pretest.kyusubkim.notice.application.manager.NoticeStatisticsManager
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeCacheManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeCacheQueryPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeQueryPort
import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeCursorPageList
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeSearchType
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NoticeQueryUseCase(
    private val noticeCacheQueryPort: NoticeCacheQueryPort,
    private val noticeCacheManagementPort: NoticeCacheManagementPort,
    private val noticeQueryPort: NoticeQueryPort,
    private val noticeStatisticsManager: NoticeStatisticsManager,
) {
    fun getNotice(noticeId: Long): Notice {
        val notice = noticeCacheQueryPort.getNotice(noticeId) ?: noticeQueryPort.getNotice(noticeId)
            ?.also { noticeCacheManagementPort.setNotice(it) }
        ?: throw RSupportBadRequestException("존재하지 않는 공지사항입니다. id - $noticeId")

        noticeStatisticsManager.increaseNoticeViewCount(noticeId)

        return notice.copy(
            viewCount = notice.viewCount + 1,
        )
    }

    fun getNoticeList(
        searchType: NoticeSearchType? = null,
        searchKeyword: String? = null,
        pageSize: Int,
        cursor: Long? = null,
    ): NoticeCursorPageList {
        val now = LocalDateTime.now()
        val visibleNoticeIds = noticeCacheQueryPort.getVisibleNoticeIds().ifEmpty {
            noticeCacheManagementPort.setVisibleNoticeIds(
                noticeIds = noticeQueryPort.getNoticeList(
                    endsAtFrom = now,
                    pageable = PageRequest.of(
                        0,
                        Integer.MAX_VALUE,
                        Sort.by(Sort.Order.desc("id"))
                    ),
                ).list.map { it.id }
            )
        }

        val noticesFromCache = noticeCacheQueryPort.getNotices(visibleNoticeIds)
        val notExistIdsInCache = visibleNoticeIds - noticesFromCache.map { it.id }.toSet()
        val noticesFromDb = if (notExistIdsInCache.isNotEmpty()) {
            noticeQueryPort.getNoticeListByIds(notExistIdsInCache)
                .also { noticeCacheManagementPort.setNoticeList(it) }
        } else {
            emptyList()
        }

        val notices = (noticesFromCache + noticesFromDb)
            .filter { it.isVisibleAt(dateTime = now) }
            .filter {
                if (searchType == null || searchKeyword.isNullOrBlank()) {
                    true
                } else {
                    when (searchType) {
                        NoticeSearchType.TITLE_AND_CONTENT -> it.title.contains(searchKeyword) || it.content.contains(searchKeyword)
                        NoticeSearchType.TITLE -> it.title.contains(searchKeyword)
                    }
                }
            }
            .sortedByDescending { it.id }

        return NoticeCursorPageList(
            hasNext = notices.size > pageSize,
            totalPages = notices.size / pageSize + if (notices.size % pageSize > 0) 1 else 0,
            totalCount = notices.size.toLong(),
            list = notices.filter { cursor == null || it.id > cursor }
                .take(pageSize),
            nextCursor = notices.lastOrNull()?.id
        )
    }
}