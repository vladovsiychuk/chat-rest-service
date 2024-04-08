package com.rest_service.saga_orchestrator.model.messageTranslate

import spock.lang.Specification

import static com.rest_service.Fixture.*
import static com.rest_service.commons.enums.SagaEventType.*
import static com.rest_service.commons.enums.ServiceEnum.*
import static com.rest_service.saga_orchestrator.model.SagaEventDSL.anEvent
import static com.rest_service.saga_orchestrator.model.messageTranslate.MessageTranslateSagaDSL.aMessageTranslateSaga
import static com.rest_service.saga_orchestrator.model.messageTranslate.MessageTranslateSagaDSL.the

class MessageTranslateSagaStateTest extends Specification {

    def 'should change the status from READY to INITIATED on MESSAGE_TRANSLATE_START event'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageTranslateSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidMessageTranslateCommand() ofType MESSAGE_TRANSLATE_START

        when:
        the userSaga reactsTo event.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_TRANSLATE_INITIATED
    }

    def 'should change the status from INITIATED to COMPLETED on MESSAGE_TRANSLATE_APPROVE event from USER_SERVICE, ROOM_SERVICE and MESSAGE_SERVICE'() {
        given: 'a user saga in INITIATED state'
        def userSaga = aMessageTranslateSaga()
        def translateEvent = anEvent() from SAGA_SERVICE withPayload anyValidMessageTranslateCommand() ofType MESSAGE_TRANSLATE_START
        the userSaga reactsTo translateEvent.event

        and:
        def approvedEventFromUserService = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType MESSAGE_TRANSLATE_APPROVED
        def approvedEventFromRoomService = anEvent() from ROOM_SERVICE withPayload anyValidRoomDTO() ofType MESSAGE_TRANSLATE_APPROVED
        def approvedEventFromMessageService = anEvent() from MESSAGE_SERVICE withPayload anyValidMessageDTO() ofType MESSAGE_TRANSLATE_APPROVED

        when:
        the userSaga reactsTo approvedEventFromUserService.event
        the userSaga reactsTo approvedEventFromRoomService.event
        the userSaga reactsTo approvedEventFromMessageService.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_TRANSLATE_COMPLETED
    }

    def 'should change the status to ERROR when processing the REJECTED event'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageTranslateSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidErrorDto() ofType MESSAGE_TRANSLATE_REJECTED

        when:
        the userSaga reactsTo event.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_TRANSLATE_ERROR
    }


    def 'should throw an exception when a not expected event is received'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageTranslateSaga()
        and:
        def event = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType USER_CREATE_APPROVED

        when:
        the userSaga reactsTo event.event

        then:
        thrown(UnsupportedOperationException)
    }
}
