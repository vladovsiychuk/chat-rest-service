package com.rest_service.messaging.user.application

import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
@Named("MessageTranslateInitiatedEventHandler_userDomain")
class MessageTranslateInitiatedEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val userStateManager: UserStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    override fun checkOperationFailed(operationId: UUID) = userStateManager.checkOperationFailed(operationId)

    override fun rebuildDomainFromEvent(event: DomainEvent): Mono<Domain> {
        event as UserDomainEvent
        return userStateManager.rebuildUser(event.userId, event.operationId)
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        return userStateManager.mapDomainEvent(event.responsibleUserId, UserDomainEventType.MESSAGE_TRANSLATE_APPROVED, event)
    }

    override fun saveEvent(event: DomainEvent): Mono<DomainEvent> {
        return userStateManager.saveEvent(event).map { it }
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return userStateManager.handleError(event, error, SagaEventType.MESSAGE_TRANSLATE_REJECTED)
    }
}
