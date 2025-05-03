package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence

import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "notice_attachment",
    catalog = "notice",
)
data class NoticeAttachmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Comment("파일 경로")
    var filePath: String,
    @ManyToOne
    @JoinColumn(name = "notice_id")
    private var notice: NoticeEntity? = null
) {
    fun update(filePath: String): NoticeAttachmentEntity {
        this.filePath = filePath
        return this
    }

    fun setParentNotice(notice: NoticeEntity): NoticeAttachmentEntity {
        this.notice = notice
        return this
    }
}