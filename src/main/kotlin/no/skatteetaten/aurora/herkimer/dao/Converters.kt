package no.skatteetaten.aurora.herkimer.dao

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@ReadingConverter
class StringToJsonNode(
    private val objectMapper: ObjectMapper
) : Converter<String, JsonNode> {
    override fun convert(source: String): JsonNode = objectMapper.readValue(source)
}

@WritingConverter
class JsonNodeToString(
    private val objectMapper: ObjectMapper
) : Converter<JsonNode, String> {
    override fun convert(source: JsonNode): String = objectMapper.writeValueAsString(source)
}

@ReadingConverter
@Component
class StringToPrincipalUID : Converter<String, PrincipalUID> {
    override fun convert(source: String): PrincipalUID = PrincipalUID.fromString(source)
}

@WritingConverter
class PrincipalUIDToString : Converter<PrincipalUID, String> {
    override fun convert(source: PrincipalUID): String = source.toString()
}
