package com.rsupport.pretest.kyusubkim.common.util

import org.springframework.http.MediaType

fun getContentTypeByFileExtension(fileName: String): MediaType {
    val extension = fileName.substringAfterLast('.', "").lowercase()

    return when (extension) {
        "pdf" -> MediaType.APPLICATION_PDF
        "jpg", "jpeg" -> MediaType.IMAGE_JPEG
        "png" -> MediaType.IMAGE_PNG
        "gif" -> MediaType.IMAGE_GIF
        "txt" -> MediaType.TEXT_PLAIN
        "html" -> MediaType.TEXT_HTML
        "json" -> MediaType.APPLICATION_JSON
        "xml" -> MediaType.APPLICATION_XML
        "csv" -> MediaType.parseMediaType("text/csv")
        "zip" -> MediaType.parseMediaType("application/zip")
        "mp3" -> MediaType.parseMediaType("audio/mpeg")
        "mp4" -> MediaType.parseMediaType("video/mp4")
        else -> MediaType.APPLICATION_OCTET_STREAM // 기본값으로 바이너리 파일
    }
}