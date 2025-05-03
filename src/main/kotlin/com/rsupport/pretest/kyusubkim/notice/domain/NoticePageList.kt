package com.rsupport.pretest.kyusubkim.notice.domain

data class NoticePageList(
    val hasNext: Boolean,
    val totalPages: Int,
    val totalCount: Long,
    val list: List<Notice>,
)