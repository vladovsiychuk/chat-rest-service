package com.rest_service.messaging.room.model

import com.rest_service.commons.Domain
import com.rest_service.commons.SagaEvent

class RoomDomainDSL {
    RoomDomain domain = new RoomDomain(UUID.fromString("423ec267-5523-448f-ad18-d3204dfa3f08"), "example@test.com", UUID.randomUUID())

    static RoomDomainDSL aRoom() {
        return new RoomDomainDSL()
    }

    static RoomDomainDSL the(RoomDomainDSL dsl) {
        return dsl
    }

    RoomDomainDSL and() {
        return this
    }

    RoomDomainDSL reactsTo(RoomDomainEventDSL event) {
        domain.apply(event.event)
        return this
    }

    RoomDomainDSL withOperationId(UUID operationId) {
        domain.operationId = operationId
        return this
    }

    RoomDomainDSL withResponsibleUserEmail(String responsibleUserEmail) {
        domain.responsibleUserEmail = responsibleUserEmail
        return this
    }

    RoomDomainDSL withResponsibleUserId(UUID userId) {
        domain.responsibleUserId = userId
        return this
    }

    SagaEvent responseEvent() {
        return Domain.createResponseSagaEvent().block()
    }
}

