package com.grupo14IngSis.snippetSearcherRunner.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class MapJsonConverter : AttributeConverter<Map<String, Any>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: Map<String, Any>?): String {
        return if (attribute.isNullOrEmpty()) {
            "{}"
        } else {
            objectMapper.writeValueAsString(attribute)
        }
    }

    override fun convertToEntityAttribute(dbData: String?): Map<String, Any> {
        return if (dbData.isNullOrBlank()) {
            emptyMap()
        } else {
            objectMapper.readValue(dbData, object : TypeReference<Map<String, Any>>() {})
        }
    }
}
