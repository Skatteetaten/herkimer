package no.skatteetaten.aurora.herkimer.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@ReadingConverter
class StringToObjectNode(
    private val objectMapper: ObjectMapper
) : Converter<String, ObjectNode> {
    override fun convert(source: String): ObjectNode = objectMapper.readValue(source)
}

@WritingConverter
class ObjectNodeToString(
    private val objectMapper: ObjectMapper
) : Converter<ObjectNode, String> {
    override fun convert(source: ObjectNode): String = objectMapper.writeValueAsString(source)
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
