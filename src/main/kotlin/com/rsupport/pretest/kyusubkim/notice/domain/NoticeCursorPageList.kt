package com.rsupport.pretest.kyusubkim.notice.domain

data class NoticeCursorPageList(
    val hasNext: Boolean,
    val totalPages: Int,
    val totalCount: Long,
    val list: List<Notice>,
    val nextCursor: Long?,
)