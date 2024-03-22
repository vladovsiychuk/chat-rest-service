package com.rest_service.websocket_service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.commons.dto.UserDTO
import com.rest_service.commons.enums.EventType
import com.rest_service.websocket.WebSocketService
import com.rest_service.websocket_service.client.ViewServiceFetcher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
open class SagaEventHandler(
    private val webSocketService: WebSocketService,
    private val viewServiceFetcher: ViewServiceFetcher
) {

    val mapper = jacksonObjectMapper()

    @EventListener
    @Async
    open fun messageActionListener(event: DomainEvent) {
        when (event.type) {
            EventType.USER_CREATE_COMPLETE    -> handleUserCreate(event)
            EventType.MESSAGE_CREATE_COMPLETE -> handleMessageUpdate(event)
            else                              -> {}
        }
    }

    private fun handleUserCreate(event: DomainEvent) {
        mapper.convertValue(event.payload, UserDTO::class.java)
            .let { user ->
                WebSocketEvent(user, WebSocketType.USER_CREATE)
                    .let { event -> mapper.writeValueAsString(event) }
                    .let { eventString -> webSocketService.sendMessageToUser(eventString, user.temporaryId!!) }
            }
    }

    private fun handleMessageUpdate(event: DomainEvent) {
        mapper.convertValue(event.payload, MessageDTO::class.java)
            .let { message ->
                Mono.zip(
                    viewServiceFetcher.getRoom(message.roomId),
                    WebSocketEvent(message, WebSocketType.MESSAGE_UPDATE).toMono()
                ) { room, event ->
                    val eventString = mapper.writeValueAsString(event)

                    room.members.forEach { roomMemberId ->
                        webSocketService.sendMessageToUser(eventString, roomMemberId)
                    }
                }
            }
    }
}
