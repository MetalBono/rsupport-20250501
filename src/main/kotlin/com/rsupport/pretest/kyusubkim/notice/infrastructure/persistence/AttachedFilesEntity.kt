package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence

import com.rsupport.pretest.kyusubkim.notice.domain.AttachedFile
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "attached_files",
    catalog = "notice",
)
data class AttachedFilesEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Comment("파일 이름")
    var name: String,
    @Comment("파일 데이터")
    @Column(columnDefinition = "MEDIUMBLOB")
    var fileData: ByteArray,
) {
    fun toDomain() = AttachedFile(
        id = this.id!!,
        name = this.name,
        fileData = this.fileData,
    )
}