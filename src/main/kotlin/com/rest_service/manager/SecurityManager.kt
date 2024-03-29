package com.rest_service.manager

import io.micronaut.context.annotation.Primary
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton

@Singleton
@Primary
open class SecurityManager(
    private val securityService: SecurityService,
) {
    fun getUserEmail(): String {
        if (!securityService.authentication.isPresent)
            throw RuntimeException("No valid credential found")

        return securityService.authentication.get().attributes["email"].toString()
    }
}
