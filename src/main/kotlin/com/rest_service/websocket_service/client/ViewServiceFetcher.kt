package com.rest_service.websocket_service.client

import com.rest_service.commons.dto.RoomDTO
import com.rest_service.commons.dto.UserDTO
import java.util.UUID
import reactor.core.publisher.Mono

interface ViewServiceFetcher {
    fun getUser(id: UUID): Mono<UserDTO>
    fun getRoom(id: UUID): Mono<RoomDTO>
}
