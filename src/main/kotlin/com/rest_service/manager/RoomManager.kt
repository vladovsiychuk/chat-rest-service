package com.rest_service.manager

import com.rest_service.domain.RoomDomain
import com.rest_service.domain.UserDomain
import com.rest_service.entity.Member
import com.rest_service.entity.Room
import com.rest_service.event.ActionEvent
import com.rest_service.event.MessageActionEvent
import com.rest_service.event.RoomActionEvent
import com.rest_service.exception.NotFoundException
import com.rest_service.exception.UnauthorizedException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.RoomRepository
import com.rest_service.resultReader.MessageResultReader
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.zip
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3

@Singleton
class RoomManager(
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
    private val messageEventRepository: MessageEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<ActionEvent>,
    private val userManager: UserManager,
) {
    fun findById(roomId: UUID, withMessages: Boolean = false): Mono<RoomDomain> {
        return zip(
            roomRepository.findById(roomId),
            memberRepository.findByRoomId(roomId).collectList(),
            if (withMessages) messageEventRepository.findProjectionByRoomId(roomId).collectList() else Mono.just(listOf())
        )
            .map { (room, members, messages) ->
                RoomDomain(room, members, messages)
            }.switchIfEmpty(NotFoundException("Room with id $roomId doesn't exist.").toMono())
    }

    fun listByUser(user: UserDomain): Flux<RoomDomain> {
        return memberRepository.findByUserId(user.toDto().id)
            .flatMap { this.findById(it.roomId, withMessages = true) }
    }

    fun createRoom(currentUserDomain: UserDomain, companionUserDomain: UserDomain): Mono<RoomDomain> {
        val currentUser = currentUserDomain.toDto()
        val companionUser = companionUserDomain.toDto()

        val room = Room(createdBy = currentUser.id)

        return roomRepository.save(room)
            .flatMap { createdRoom ->
                val firstMember = Member(roomId = createdRoom.id!!, userId = currentUser.id)
                val secondMember = Member(roomId = createdRoom.id, userId = companionUser.id)

                zip(
                    memberRepository.save(firstMember),
                    memberRepository.save(secondMember)
                )
                    .then(this.findById(createdRoom.id))
            }
    }

    fun addNewMember(room: RoomDomain, newMemberUser: UserDomain): Mono<RoomDomain> {
        val roomId = room.toDto().id
        val newMember = Member(roomId = roomId, userId = newMemberUser.toDto().id)

        return memberRepository.save(newMember)
            .then(this.findById(roomId))
    }

    fun broadcastMessageToRoomMembers(updatedRoom: RoomDomain): Mono<Boolean> {
        return updatedRoom.toDto().members.toFlux()
            .doOnNext { memberId ->
                val event = RoomActionEvent(memberId, updatedRoom.toDto())
                applicationEventPublisher.publishEventAsync(event)
            }
            .then(true.toMono())
    }

    fun broadcastMessageToRoomMembers(room: RoomDomain, message: MessageResultReader): Mono<Boolean> {
        return room.toDto().members.toFlux()
            .flatMap { userManager.findByUserId(it) }
            .doOnNext { user ->
                val messageDto = message.toDomain(user).toDto()
                val messageEvent = MessageActionEvent(user.toDto().id, messageDto)
                applicationEventPublisher.publishEventAsync(messageEvent)
            }
            .then(true.toMono())
    }

    fun validateUserIsRoomMember(user: UserDomain, room: RoomDomain): Mono<Boolean> {
        if (!room.isRoomMember(user))
            return UnauthorizedException().toMono()

        return true.toMono()
    }
}
