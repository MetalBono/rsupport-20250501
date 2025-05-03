package com.rsupport.pretest.kyusubkim.notice.application.usecase

import com.rsupport.pretest.kyusubkim.common.exception.RSupportBadRequestException
import com.rsupport.pretest.kyusubkim.notice.application.dto.CreateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.dto.UpdateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeCacheManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeQueryPort
import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import com.rsupport.pretest.kyusubkim.notice.domain.NoticePageList
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeSearchType
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NoticeManagementUseCase(
    private val noticeCacheManagementPort: NoticeCacheManagementPort,
    private val noticeManagementPort: NoticeManagementPort,
    private val noticeQueryPort: NoticeQueryPort,
) {
    @Transactional
    fun createNotice(dto: CreateNoticeRequestDto): Notice {
        val notice = noticeManagementPort.createNotice(dto)
        noticeCacheManagementPort.setNotice(notice)
        if (notice.isVisibleAt(LocalDateTime.now())) {
            noticeCacheManagementPort.addToVisibleIds(notice.id)
        }
        return notice
    }

    @Transactional
    fun updateNotice(dto: UpdateNoticeRequestDto): Notice {
        val notice = noticeManagementPort.updateNotice(dto)
        noticeCacheManagementPort.setNotice(notice)
        if (notice.isVisibleAt(LocalDateTime.now())) {
            noticeCacheManagementPort.addToVisibleIds(notice.id)
        } else {
            noticeCacheManagementPort.deleteFromVisibleIds(notice.id)
        }
        return notice
    }

    @Transactional
    fun deleteNotice(noticeId: Long) {
        noticeManagementPort.deleteNotice(noticeId)
        noticeCacheManagementPort.deleteNotice(noticeId)
        noticeCacheManagementPort.deleteFromVisibleIds(noticeId)
    }

    fun getNotice(noticeId: Long): Notice {
        return noticeQueryPort.getNotice(noticeId)
            ?: throw RSupportBadRequestException("존재하지 않는 공지사항입니다. id - $noticeId")
    }

    fun getNoticeList(
        searchType: NoticeSearchType? = null,
        searchKeyword: String? = null,
        createdAtFrom: LocalDateTime? = null,
        createdAtTo: LocalDateTime? = null,
        pageable: Pageable,
    ): NoticePageList {
        return noticeQueryPort.getNoticeList(
            searchType = searchType,
            searchKeyword = searchKeyword,
            createdAtFrom = createdAtFrom,
            createdAtTo = createdAtTo,
            pageable = PageRequest.of(
                pageable.pageNumber,
                pageable.pageSize,
                Sort.by(Sort.Order.desc("id"))
            ),
        )
    }
}