package com.rest_service.dto

import com.rest_service.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class TranslationDTO(
    val translatorId: UUID,
    val translation: String,
    val language: LanguageEnum,
)
