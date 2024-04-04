package com.rest_service.messaging.room.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.ErrorDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventRepository
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType
import com.rest_service.messaging.room.model.RoomDomain
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
@Named("RoomCreateInitiatedEventHandler_roomDomain")
class RoomAddMemberInitiatedEventHandler(
    private val repository: RoomDomainEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
) : AbstractEventHandler(applicationEventPublisher) {
    private val mapper = jacksonObjectMapper()
    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        val operationId = event.operationId

        return repository.existsByOperationIdAndType(operationId, RoomDomainEventType.UNDO)
            .flatMap { operationFailed ->
                if (operationFailed)
                    return@flatMap Mono.empty()

                RoomDomain(operationId, event.responsibleUserEmail, event.responsibleUserId!!).toMono()
            }
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        return RoomDomainEvent(
            roomId = UUID.randomUUID(),
            payload = mapper.convertValue(event.payload),
            type = RoomDomainEventType.ROOM_MEMBER_ADDED,
            operationId = event.operationId,
            responsibleUserId = event.responsibleUserId!!
        )
    }

    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return repository.save(event as RoomDomainEvent)
            .map { true }
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return repository.existsByOperationIdAndType(event.operationId, RoomDomainEventType.UNDO)
            .flatMap { exists ->
                if (exists)
                    return@flatMap Mono.empty<Void>()

                val errorEvent = SagaEvent(
                    SagaEventType.ROOM_ADD_MEMBER_REJECTED,
                    event.operationId,
                    ServiceEnum.ROOM_SERVICE,
                    event.responsibleUserEmail,
                    event.responsibleUserId!!,
                    ErrorDTO(error.message),
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }
}