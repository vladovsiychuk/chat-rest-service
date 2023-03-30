package com.rest_service.repository

import com.rest_service.domain.Member
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

@R2dbcRepository(dialect = Dialect.MYSQL)
interface MemberRepository : ReactorCrudRepository<Member, UUID> {
    fun findByUserId(userId: UUID): Flux<Member>
}
