package com.rest_service.controller

import com.rest_service.dto.MessageDTO
import com.rest_service.service.MessageService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import reactor.core.publisher.Flux
import java.util.UUID

@Controller("/v1/messages")
class MessageController(private val service: MessageService) {

    @Secured("isAuthenticated()")
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun index(@QueryValue roomId: UUID): Flux<MessageDTO> {
        return service.list(roomId)
    }
}