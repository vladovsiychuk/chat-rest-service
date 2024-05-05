## Table of contents

1. [Introduction](#introduction)  
   1.1 [Project Overview](#11-project-overview)  
   1.2 [Key Features](#12-key-features)  
   1.3 [Technologies Used](#13-technologies-used)

2. [Architecture](#2-architecture)  
   2.1 [System Architecture](#21-system-architecture)  
   2.2 [Saga Orchestration](#22-saga-orchestration)  
   2.3 Domain-Driven Design Overview  
   2.4 Event Sourcing Details  
   2.5 Reactive Programming Approach

3. API Documentation  
   3.1 Endpoints  
   3.2 Usage Examples

4. WebSocket Communication  
   4.1 Event Types  
   4.2 Usage and Examples

5. Database Schema  
   5.1 Diagrams  
   5.2 Descriptions

6. Testing  
   6.1 Unit Tests  
   6.2 Integration Tests

7. Deployment  
   7.1 Recommended Deployment Practices  
   7.2 Environment Setup

8. Incomplete Features  
   8.1 Current Limitations  
   8.1 Planned Features

9. Contributing  
   9.1 How to Contribute  
   9.2 Code of Conduct  
   9.3 Contributors

9. License

10. Contact Information

## 1. Introduction

### 1.1 Project Overview

Our application is an advanced chat platform designed to enable seamless communication among users who speak different languages. This platform supports real-time messaging and integrates human translators directly into the chat, allowing for accurate, context-aware translations that maintain the nuance of each conversation. It is ideal for environments requiring precise cross-language
communication, such as multinational organizations, global customer service teams, and diverse project collaborations.

The system allows for two types of user registrations: Regular users can sign up and choose their preferred language, while Translator users select a list of languages they can translate. Users can create rooms, add other users, and designate translators who can translate messages within these rooms.

### 1.2 Key Features

- **Real-time Messaging**: Users can send and receive messages instantly in designated chat rooms.
- **Human-Powered Translations**: Dedicated human translators provide accurate translations, ensuring clarity and context are preserved.
- **Dynamic User Roles**: Two distinct user roles with specific capabilities:
- **Regular Users**: Can participate in rooms and receive translations.
- **Translator Users**: Can provide translations for messages in their proficient languages.
- **Multi-language Support**: Each message can be translated into multiple languages, allowing participants to interact in their native language without barriers.
- **Unique Translator Functionality**: Translators can only add translations for languages they are proficient in and cannot add a translation to a message if it already exists. This ensures that translations are provided by qualified individuals and that messages do not have redundant translations.

### 1.3 Technologies Used

- **Domain-Driven Design**: Structures around the business domain, making it easier to understand and align with business requirements.
- **Saga Orchestration**: Ensures that all steps in a business transaction are completed successfully or compensated if any step fails.
- **Event Sourcing and CQRS**: Implements event sourcing to reliably manage state changes and utilizes CQRS for enhanced performance in read operations
- **Reactive Programming**: Employs Project Reactor to facilitate asynchronous and non-blocking operations.
- **WebSocket Communication**: Enables real-time communication between the web interface and the server.
- **Micronaut Framework**: Utilized for building scalable, efficient microservice architectures.
- **Kotlin**: The primary language for building the application, offering conciseness and safety.
- **Jackson**: For JSON serialization and deserialization.
- **PostgreSQL/MySQL**: As the database system, depending on the deployment.

The `saga-orchestrator-ddd-chat` is an advanced chat application that leverages Domain-Driven Design (DDD) principles and Saga orchestration to provide a robust solution for real-time messaging. This project is designed to showcase how complex business transactions (sagas) that span multiple microservices can be coordinated in a reactive and event-driven architecture. It's ideal for developers
looking to understand the implementation of sagas in microservices architecture or those developing complex systems requiring reliable communication mechanisms.

## 2. Architecture

The architecture of this project is designed to accommodate complex business processes within the domain of messaging and chat operations, using a flexible and scalable approach that leverages Domain-Driven Design (DDD), event sourcing, and reactive programming principles. While structured as a monolithic application for ease of development and deployment, it maintains a clear separation of
concerns through its modular design, enabling potential evolution into a microservices architecture.
Each core component of the system — including `saga_orchestrator`, `user`, `room`, and `message` — encapsulates its own business logic and state management, coordinated through a centralized saga orchestration mechanism that ensures transactional consistency across various operations. This design allows the system to handle complex workflows such as user registration, room management, and message
handling
in a cohesive and robust manner.
The adoption of event sourcing as a fundamental architectural pattern not only enables the system to preserve a complete history of all changes but also provides the flexibility to respond to future requirements and scaling needs. Coupled with a reactive programming approach, the system is well-equipped to handle a high volume of messages with efficiency and resilience, providing real-time
feedback and interactions to the users.
In the following sections, we'll dive deeper into the individual architectural components and their roles within the larger system.

### 2.1 System Architecture

The system architecture is conceived as a unified platform that supports real-time messaging and chat functionalities. It is a confluence of distinct yet interrelated modules that operate both independently and collaboratively, forming a cohesive ecosystem for messaging services. The following diagram provides a visual overview of the system's architectural design:
![Architecture](docs/images/architecture.png)

- **Core Domains**  
  At the heart of the architecture are the core domain components:
    - `User`: Manages user-related operations such as registration and profile management.
    - `Room`: Handles room-related functionalities including creation, membership management, and room settings.
    - `Message`: Takes care of all aspects of messaging, from sending and receiving messages to message translation and status updates.

  These domains are designed following DDD principles, allowing for a clear separation of concerns and focused domain models.


- **Saga Orchestration**  
  The `saga_orchestrator` acts as the central coordinator for distributed transactions and complex business processes. It ensures data consistency and orchestrates the flow of events across different domain boundaries.


- **Infrastructure Services**  
  Infrastructure services provide support functionalities such as:

    - `read_service`: Responsible for read operations, storing and retrieving the state of domain objects for query operations.
    - `websocket_service`: Handles real-time communication with connected clients, ensuring timely updates and notifications are pushed via websockets.


- **Data Persistence**  
  Event sourcing is employed to persist the state changes as a sequence of events. Each core domain has its event table (`saga_event`, `user_domain_event`, `room_domain_event`, `message_domain_event`) which records all domain events that have occurred.


- **View Models**  
  The read side of the system is represented by view models (`user_view`, `room_view`, `message_view`, `room_members`) that are optimized for queries and provide the necessary data for the read service and other query operations.

This architecture provides a robust foundation for scaling, maintenance, and future enhancements.

### 2.2 Saga Orchestration

The saga orchestration mechanism is a critical aspect of the system architecture, designed to manage distributed transactions across various bounded contexts through a state machine implemented in the `AbstractSagaStateManager` class. This orchestration ensures that all transactions are consistently and reliably handled, leveraging event sourcing to maintain and rebuild the state of each saga.
The diagram below
illustrates the transitions and states:
![Saga State Machine](docs/images/saga-state-machine.png)

- **The Saga State Machine**  
  The state machine at the core of saga orchestration initiates in a READY state and transitions through various stages based on the flow of events. It progresses to INITIATED upon a START event, broadcasting INITIATED event to relevant domains. Each domain processes this initiation asynchronously and in parallel and responds with either APPROVED or REJECTED events.


- **Handling Events and State Transitions**  
  In the IN_APPROVING state, the saga orchestrator verifies transaction completion by checking for the necessary approvals from all involved services. If the conditions are met, it transitions to the COMPLETE state and issues a COMPLETED event. Conversely, upon receiving a REJECTED event, the saga orchestrator moves to an error state and emits an ERROR event, which is consumed only
  by `websocket_service`.


- **Compensation Logic and Error Handling**  
  The REJECTED event is critical as it is consumed not only by the `saga_orchestrator` but also by all participating domains. This triggers compensation logic across the system, where necessary rollback or corrective actions are taken. The `saga _orchestrator` additionally emits an ERROR event, which is specifically consumed by the `websocket_service` to relay error information back to the
  user,
  ensuring transparency and responsiveness.


- **Integration with Event Sourcing**  
  Employing event sourcing, the `saga_orchestrator` updates and rebuilds the state of each transaction dynamically, based on the events processed. This approach enhances the resilience and scalability of the system by decoupling state management from the transactional operations, thereby allowing for more robust error handling and recovery mechanisms.


- **Collaboration with read_service and websocket_service**  
  Both `read_service` and `websocket_service` play crucial roles in handling the COMPLETED and ERROR events. While `read_service` updates views and read models ensuring data consistency, `websocket_service` facilitates real-time communication with users, enhancing the interactive experience by providing timely updates on the status of transactions.

- **Customization for Specific Domains**  
  Through the abstract implementation provided by `AbstractSagaStateManager`, each saga can be customized for specific domain needs, defining its unique command, dto, events, and completion logic. For example, the `RoomCreateSaga` requires approvals from `room_service` and `user_service` to conclude successfully.

```kotlin
class RoomCreateSaga(
    val operationId: UUID,
    private val responsibleUserId: UUID,
) : AbstractSagaStateManager<RoomCreateCommand, RoomDTO>() {
    override fun startEvent() = SagaEventType.ROOM_CREATE_START
    override fun approveEvent() = SagaEventType.ROOM_CREATE_APPROVED
    override fun rejectEvent() = SagaEventType.ROOM_CREATE_REJECTED

    override fun isComplete() = approvedServices.containsAll(
        listOf(
            ServiceEnum.ROOM_SERVICE, ServiceEnum.USER_SERVICE
        )
    )

    override fun mainDomainService() = ServiceEnum.ROOM_SERVICE

    override fun createInitiatedResponseEvent() =
        SagaEvent(SagaEventType.ROOM_CREATE_INITIATED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, command)

    override fun createCompletedResponseEvent() =
        SagaEvent(SagaEventType.ROOM_CREATE_COMPLETED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, dto)

    override fun createErrorResponseEvent() =
        SagaEvent(SagaEventType.ROOM_CREATE_ERROR, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, errorDto!!)

    ...
}
```

### 2.3 Domain-Driven Design Overview

In the saga-orchestrator-ddd-chat application, each core domain—"user," "room," and "message"—adheres to a hexagonal architecture model, comprising three primary layers: the Infrastructure layer, Application layer, and Domain layer. This structure supports the principles of Domain-Driven Design by emphasizing clear boundaries and focused responsibilities within each domain.

**Hexagonal Architecture**  
Each domain is structured into three layers:

- **Infrastructure Layer**: Primarily handles data persistence.


- **Application Layer**: Serves as the bridge between the domain logic and external interfaces, managing the flow of data to and from the domain and external clients or services. In this layer, after handling a saga event and transforming it into a domain command, it rebuilds the domain model from events stored in the database (event sourcing) and then sends the saga response event. This
  operational flow is encapsulated in the **AbstractEventHandler** class, which provides a structured process to manage domain events through a series of steps:

    1. Mapping the incoming saga event to a domain-specific event.
    2. Persisting the event.
    3. Rebuilding the domain entity from persisted events.
    4. Generating and sending response saga events.
    5. Handling errors.

    ``` kotlin
    abstract class AbstractEventHandler(private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>) {
        protected abstract fun rebuildDomainFromEvent(domainEvent: DomainEvent): Mono<Domain>
        protected abstract fun mapDomainEvent(): Mono<DomainEvent>
        protected abstract fun saveEvent(domainEvent: DomainEvent): Mono<DomainEvent>
        protected abstract fun handleError(error: Throwable): Mono<Void>
        protected abstract fun createResponseSagaEvent(domain: Domain): Mono<SagaEvent>
    
        fun handleEvent() {
            mapDomainEvent()
                .flatMap { domainEvent -> saveEvent(domainEvent) }
                .flatMap { savedEvent -> rebuildDomainFromEvent(savedEvent) }
                .flatMap { domain -> createResponseSagaEvent(domain) }
                .doOnNext { responseEvent -> applicationEventPublisher.publishEventAsync(responseEvent) }
                .then()
                .onErrorResume { handleError(it) }
                .subscribe()
        }
    }
    ```

  Each step ensures that the domain remains consistent and accurately represents the current state based on the sequence of events it has processed. This method facilitates a clear and maintainable way to handle changes within the domain driven by external commands.


- **Domain Layer**: Contains the core business logic and domain models. It is responsible for applying business rules and ensuring data consistency and validity. Domain entities in this layer react to commands by changing state and raising events which are handled within the same domain or by other domains.An example implementation in the Domain layer would be:

    ```kotlin
    class User : Domain {
        private var status = UserStatus.IN_CREATION
        private lateinit var user: UserData
    
        private val mapper = jacksonObjectMapper()
    
        fun apply(event: UserDomainEvent): DomainEvent {
            when (event.type) {
                UserDomainEventType.USER_CREATED               -> handleUserCreated(event)
                UserDomainEventType.MESSAGE_TRANSLATE_APPROVED -> approveMessageTranslate(event)
    
                UserDomainEventType.ROOM_CREATE_APPROVED,
                UserDomainEventType.ROOM_ADD_MEMBER_APPROVED,
                UserDomainEventType.MESSAGE_UPDATE_APPROVED,
                UserDomainEventType.MESSAGE_READ_APPROVED      -> checkForUserCreatedStatus()
    
                else                                           -> {}
            }
    
            return event
        }
    
        private fun handleUserCreated(event: UserDomainEvent) {
            checkForUserInCreationStatus()
            val command = mapper.convertValue(event.payload, UserCreateCommand::class.java)
    
            if (UUID.nameUUIDFromBytes(command.email.toByteArray()) != event.responsibleUserId)
                throw RuntimeException("Responsible user doesn't have permissions to create the user")
    
            user = UserData(
                event.userId,
                command.username,
                command.email,
                null,
                command.primaryLanguage,
                if (command.type == UserType.TRANSLATOR) TranslationLanguages.from(command) else null,
                command.type,
                event.dateCreated,
                event.dateCreated,
            )
    
            status = UserStatus.CREATED
        }
    
        private fun approveMessageTranslate(event: UserDomainEvent) {
            checkForUserCreatedStatus()
    
            val command = mapper.convertValue(event.payload, MessageTranslateCommand::class.java)
    
            when {
                user.type != UserType.TRANSLATOR                        ->
                    throw RuntimeException("User with id ${user.id} is not a translator.")
    
                !user.translationLanguages!!.contains(command.language) ->
                    throw RuntimeException("User with id ${user.id} cannot translate ${command.language}")
            }
        }
    
        private fun checkForUserCreatedStatus() {
            if (status != UserStatus.CREATED)
                throw RuntimeException("User is not yet created.")
        }
    
        private fun checkForUserInCreationStatus() {
            if (status != UserStatus.IN_CREATION)
                throw RuntimeException("User is already created.")
        }
    
        override fun toDto(): UserDTO {
            return UserDTO(
                user.id,
                user.username,
                user.email,
                user.avatar,
                user.primaryLanguage,
                user.translationLanguages?.languages,
                user.type,
                user.dateCreated,
                user.dateUpdated,
            )
        }
    
        private data class UserData(
            val id: UUID,
            val username: String?,
            val email: String,
            val avatar: String?,
            val primaryLanguage: LanguageEnum,
            val translationLanguages: TranslationLanguages?,
            val type: UserType,
            val dateCreated: Long,
            val dateUpdated: Long,
        )
    
        private enum class UserStatus {
            IN_CREATION,
            CREATED
        }
    }
    ```

  The Domain layer's models are designed to be rebuilt from a series of events, adhering to the event sourcing pattern, which will be detailed in the "2.4 Event Sourcing Details" section.
