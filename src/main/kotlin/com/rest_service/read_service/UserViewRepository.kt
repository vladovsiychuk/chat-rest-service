package com.rest_service.read_service

import com.rest_service.read_service.entity.UserView
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface UserViewRepository : ReactorCrudRepository<UserView, UUID> {
//    fun findById(id: UUID): Mono<UserView>
    fun update(user: UserView): Mono<UserView>
}