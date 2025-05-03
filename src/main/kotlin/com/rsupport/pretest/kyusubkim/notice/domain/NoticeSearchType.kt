package com.rsupport.pretest.kyusubkim.notice.domain

enum class NoticeSearchType(
    val description: String,
) {
    TITLE_AND_CONTENT("제목 + 내용"),
    TITLE("제목"),
    ;
}