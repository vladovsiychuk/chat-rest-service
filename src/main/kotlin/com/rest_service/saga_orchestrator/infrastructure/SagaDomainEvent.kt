package com.rest_service.saga_orchestrator.infrastructure

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.time.Instant
import java.util.UUID

@MappedEntity
data class SagaDomainEvent(
    @field:Id
    @AutoPopulated
    val id: UUID? = null,
    var operationId: UUID,
    @MappedProperty(type = DataType.JSON)
    var payload: Map<String, Any>,
    var responsibleService: ServiceEnum,
    val responsibleUserEmail: String,
    val responsibleUserId: UUID? = null,
    var type: SagaEventType,
    val dateCreated: Long = Instant.now()
        .toEpochMilli(),
) : DomainEvent