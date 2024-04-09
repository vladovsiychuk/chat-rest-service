package com.rest_service.read_service

import com.rest_service.read_service.entity.RoomView
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface RoomViewRepository : ReactorCrudRepository<RoomView, UUID> {
    fun update(room: RoomView): Mono<RoomView>
}
