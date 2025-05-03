package com.rsupport.pretest.kyusubkim.notice.domain

data class AttachedFile(
    val id: Long,
    val name: String,
    val fileData: ByteArray,
)