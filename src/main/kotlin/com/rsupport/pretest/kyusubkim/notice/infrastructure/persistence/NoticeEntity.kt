package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence

import jakarta.persistence.*
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Entity
@Table(
    name = "notice",
    catalog = "notice",
)
data class NoticeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    @Comment("제목")
    var title: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("내용")
    var content: String,
    @Column(nullable = false)
    @Comment("공지사항 시작 일시")
    var startsAt: LocalDateTime,
    @Column(nullable = false)
    @Comment("공지사항 종료 일시")
    var endsAt: LocalDateTime,
    @Column(nullable = false)
    @Comment("조회 수")
    var viewCount: Long,
    @Column(nullable = false)
    @Comment("등록 일시")
    val createdAt: LocalDateTime,
    @Column(nullable = false)
    @Comment("등록자")
    val createdBy: String,
    @Column(nullable = true)
    @Comment("최종 수정 일시")
    var updatedAt: LocalDateTime? = null,
    @Column(nullable = true)
    @Comment("최종 수정자")
    var updatedBy: String? = null,
    @BatchSize(size = 10)
    @OneToMany(
        mappedBy = "notice",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    @OrderBy("id asc")
    var attachments: MutableList<NoticeAttachmentEntity>? = null
) {
    fun updateBy(
        title: String,
        content: String,
        startsAt: LocalDateTime,
        endsAt: LocalDateTime,
        updatedBy: String,
    ): NoticeEntity {
        this.title = title
        this.content = content
        this.startsAt = startsAt
        this.endsAt = endsAt
        this.updatedAt = LocalDateTime.now()
        this.updatedBy = updatedBy
        return this
    }

    fun addAttachments(attachmentEntities: List<NoticeAttachmentEntity>?): NoticeEntity {
        if (this.attachments == null) {
            this.attachments = mutableListOf()
        }

        if (!attachmentEntities.isNullOrEmpty()) {
            attachmentEntities.forEach { it.setParentNotice(this) }
            this.attachments!!.addAll(attachmentEntities)
        }
        return this
    }

    fun deleteAttachmentsBy(deleteTargetIds: List<Long>): NoticeEntity {
        this.attachments
        if (!this.attachments.isNullOrEmpty() && deleteTargetIds.isNotEmpty()) {
            this.attachments?.removeIf { attachement ->
                deleteTargetIds.contains(
                    attachement.id
                )
            }
        }
        return this
    }
}