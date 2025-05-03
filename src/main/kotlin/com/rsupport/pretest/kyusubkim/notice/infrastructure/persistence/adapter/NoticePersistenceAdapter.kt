package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.adapter

import com.rsupport.pretest.kyusubkim.common.exception.RSupportBadRequestException
import com.rsupport.pretest.kyusubkim.notice.application.dto.CreateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.dto.UpdateNoticeRequestDto
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeManagementPort
import com.rsupport.pretest.kyusubkim.notice.application.port.NoticeQueryPort
import com.rsupport.pretest.kyusubkim.notice.domain.Notice
import com.rsupport.pretest.kyusubkim.notice.domain.NoticePageList
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeSearchType
import com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.*
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class NoticePersistenceAdapter(
    private val noticeRepository: NoticeRepository,
    private val noticeAttachmentRepository: NoticeAttachmentRepository,
) : NoticeManagementPort, NoticeQueryPort {
    @Transactional
    override fun createNotice(dto: CreateNoticeRequestDto): Notice {
        return noticeRepository.save(
            NoticeEntity(
                title = dto.title,
                content = dto.content,
                startsAt = dto.startsAt,
                endsAt = dto.endsAt,
                viewCount = 0,
                createdAt = LocalDateTime.now(),
                createdBy = dto.createdBy,
            ).addAttachments(dto.attachments?.map { it.toEntity() }?.toMutableList())
        ).toDomain()
    }

    @Transactional
    override fun updateNotice(dto: UpdateNoticeRequestDto): Notice {
        val noticeEntity = noticeRepository.findById(dto.id)
            .orElseThrow {
                throw RSupportBadRequestException("존재하지 않는 공지사항입니다. id - ${dto.id}")
            }
        noticeEntity.updateBy(
            title = dto.title,
            content = dto.content,
            startsAt = dto.startsAt,
            endsAt = dto.endsAt,
            updatedBy = dto.updatedBy,
        )

        val currentAttachments = noticeEntity.attachments ?: emptyList()
        val insertTargetEntities = mutableListOf<NoticeAttachmentEntity>()
        val deleteTargetIds = mutableListOf<Long>()
        if (!dto.attachments.isNullOrEmpty()) {
            for (attachment in dto.attachments) {
                if (attachment.id == null) {
                    // ID 가 없는 요청은 신규 추가
                    insertTargetEntities.add(
                        NoticeAttachmentEntity(
                            filePath = attachment.filePath,
                        )
                    )
                } else {
                    val matchedAttachment = currentAttachments
                        .firstOrNull { entity -> entity.id == attachment.id }
                    if (matchedAttachment != null) {
                        // 매칭되는 ID 가 있으면 업데이트 처리
                        matchedAttachment.update(
                            filePath = attachment.filePath,
                        )
                    } else {
                        // 매칭되는 ID 가 없는 항목이므로 삭제 대상
                        deleteTargetIds.add(attachment.id)
                    }
                }
            }

            // 갱신 목록에 없는 데이터는 삭제 대상으로 추가
            currentAttachments
                .filter {
                    dto.attachments
                        .none { attachment -> attachment.id == it.id }
                }
                .forEach { deleteTargetIds.add(it.id!!) }
        } else {
            // 업데이트 대상 목록이 비어있으므로 모두 삭제
            deleteTargetIds.addAll(currentAttachments.map { it.id!! })
        }

        // 삭제
        if (deleteTargetIds.isNotEmpty()) {
            noticeEntity.deleteAttachmentsBy(deleteTargetIds)
        }

        // 추가
        if (insertTargetEntities.isNotEmpty()) {
            val insertedEntities = noticeAttachmentRepository.saveAll(insertTargetEntities)
            noticeEntity.addAttachments(insertedEntities)
        }
        return noticeEntity.toDomain()
    }

    @Transactional
    override fun deleteNotice(noticeId: Long) {
        noticeRepository.deleteById(noticeId)
    }

    @Transactional
    override fun increaseNoticeViewCount(noticeId: Long, viewCount: Long) {
        noticeRepository.incrementViewCount(noticeId, viewCount)
    }

    override fun getNotice(noticeId: Long): Notice? {
        return noticeRepository.findById(noticeId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun getNoticeList(
        searchType: NoticeSearchType?,
        searchKeyword: String?,
        createdAtFrom: LocalDateTime?,
        createdAtTo: LocalDateTime?,
        endsAtFrom: LocalDateTime?,
        pageable: Pageable
    ): NoticePageList {
        return noticeRepository.findAllBy(
            searchType = searchType,
            searchKeyword = searchKeyword,
            createdAtFrom = createdAtFrom,
            createdAtTo = createdAtTo,
            endsAtFrom = endsAtFrom,
            pageable = pageable,
        ).map { it.toDomain() }
            .let {
                NoticePageList(
                    hasNext = it.hasNext(),
                    totalPages = it.totalPages,
                    totalCount = it.totalElements,
                    list = it.content,
                )
            }
    }

    override fun getNoticeListByIds(noticeIds: List<Long>): List<Notice> {
        return noticeRepository.findAllById(noticeIds)
            .map { it.toDomain() }
    }
}