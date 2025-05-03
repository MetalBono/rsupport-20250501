package com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.rsupport.pretest.kyusubkim.notice.domain.NoticeSearchType
import com.rsupport.pretest.kyusubkim.notice.infrastructure.persistence.QNoticeEntity.noticeEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class NoticeRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : NoticeRepositoryCustom {
    override fun findAllBy(
        searchType: NoticeSearchType?,
        searchKeyword: String?,
        createdAtFrom: LocalDateTime?,
        createdAtTo: LocalDateTime?,
        endsAtFrom: LocalDateTime?,
        endsAtTo: LocalDateTime?,
        pageable: Pageable
    ): Page<NoticeEntity> {
        val whereClause = BooleanBuilder()
            .and(searchKeywordLikeByType(
                searchType = searchType,
                searchKeyword = searchKeyword,
            ))
            .and(createdAtGte(createdAtFrom))
            .and(createdAtLte(createdAtTo))
            .and(endsAtGte(endsAtFrom))
            .and(endsAtLte(endsAtTo))

        val results = queryFactory.selectFrom(noticeEntity)
            .where(whereClause)
            .orderBy(*getOrderSpecifiers(pageable))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val totalCount = queryFactory.select(noticeEntity.count())
            .from(noticeEntity)
            .where(whereClause)
            .fetchOne()

        return PageImpl(results, pageable, (totalCount ?: 0).toLong())
    }

    private fun searchKeywordLikeByType(searchType: NoticeSearchType?, searchKeyword: String?): BooleanExpression? {
        return if (searchType != null && !searchKeyword.isNullOrBlank()) {
            when (searchType) {
                NoticeSearchType.TITLE_AND_CONTENT -> noticeEntity.title.contains(searchKeyword).or(noticeEntity.content.contains(searchKeyword))
                NoticeSearchType.TITLE -> noticeEntity.title.contains(searchKeyword)
            }
        } else {
            null
        }
    }

    private fun createdAtGte(createdAtFrom: LocalDateTime?): BooleanExpression? {
        return createdAtFrom?.let { noticeEntity.createdAt.goe(createdAtFrom) }
    }

    private fun createdAtLte(createdAtTo: LocalDateTime?): BooleanExpression? {
        return createdAtTo?.let { noticeEntity.createdAt.loe(createdAtTo) }
    }

    private fun endsAtGte(endsAtFrom: LocalDateTime?): BooleanExpression? {
        return endsAtFrom?.let { noticeEntity.endsAt.goe(endsAtFrom) }
    }

    private fun endsAtLte(endsAtTo: LocalDateTime?): BooleanExpression? {
        return endsAtTo?.let { noticeEntity.endsAt.loe(endsAtTo) }
    }

    private fun getOrderSpecifiers(pageable: Pageable): Array<OrderSpecifier<*>> {
        return pageable.sort
            .map { order: Sort.Order ->
                val path = Expressions.path(Comparable::class.java, noticeEntity, order.property)
                val direction = if (order.isAscending) Order.ASC else Order.DESC
                OrderSpecifier(direction, path)
            }.toList().toTypedArray()
    }
}