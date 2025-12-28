package kotlinx.serialization.gson

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.SerializersModule

class GsonDecoder(private val element: JsonElement) : Decoder, CompositeDecoder {

    override val serializersModule: SerializersModule = SerializersModule {}
    private var currentIndex = 0
    private val jsonObject: JsonObject? = element.asJsonObjectOrNull()
    private val jsonArray: JsonArray? = element.asJsonArrayOrNull()

    private fun JsonElement?.asJsonObjectOrNull(): JsonObject? = this?.takeIf { it.isJsonObject }?.asJsonObject
    private fun JsonElement?.asJsonArrayOrNull(): JsonArray? = this?.takeIf { it.isJsonArray }?.asJsonArray
    private fun getElementForIndex(descriptor: SerialDescriptor, index: Int): JsonElement {
        return jsonObject?.get(descriptor.getElementName(index)) ?: JsonNull.INSTANCE
    }

    // ------------------- CompositeDecoder -------------------
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return GsonDecoder(element)
    }
    override fun endStructure(descriptor: SerialDescriptor) {}

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (jsonObject != null) {
            while (currentIndex < descriptor.elementsCount) {
                val name = descriptor.getElementName(currentIndex)
                if (jsonObject.has(name)) return currentIndex++
                currentIndex++
            }
            return CompositeDecoder.DECODE_DONE
        } else if (jsonArray != null) {
            if (currentIndex < jsonArray.size()) return currentIndex++
            return CompositeDecoder.DECODE_DONE
        } else return CompositeDecoder.DECODE_DONE
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val jsonElem = if (jsonObject != null) {
            jsonObject[descriptor.getElementName(index)] ?: JsonNull.INSTANCE
        } else if (jsonArray != null) {
            jsonArray[index]
        } else JsonNull.INSTANCE

        return deserializer.deserialize(GsonDecoder(jsonElem))
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        val jsonElem = if (jsonObject != null) {
            jsonObject[descriptor.getElementName(index)] ?: JsonNull.INSTANCE
        } else if (jsonArray != null) {
            jsonArray[index]
        } else JsonNull.INSTANCE
        return GsonDecoder(jsonElem)
    }

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        val jsonElem = if (jsonObject != null) {
            jsonObject[descriptor.getElementName(index)] ?: JsonNull.INSTANCE
        } else if (jsonArray != null) {
            jsonArray[index]
        } else JsonNull.INSTANCE
        return if (jsonElem is JsonNull) null else deserializer.deserialize(GsonDecoder(jsonElem))
    }

    // ------------------- Decoder for single values -------------------
    private fun element(): JsonElement = element

    override fun decodeString(): String = element().asString
    override fun decodeInt(): Int = element().asInt
    override fun decodeLong(): Long = element().asLong
    override fun decodeDouble(): Double = element().asDouble
    override fun decodeFloat(): Float = element().asFloat
    override fun decodeBoolean(): Boolean = element().asBoolean
    override fun decodeByte(): Byte = element().asByte
    override fun decodeShort(): Short = element().asShort
    override fun decodeChar(): Char = element().asString[0]

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte = getElementForIndex(descriptor, index).asByte

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        getElementForIndex(descriptor, index).asString[0]

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        getElementForIndex(descriptor, index).asDouble

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        getElementForIndex(descriptor, index).asFloat

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        getElementForIndex(descriptor, index).asShort

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        getElementForIndex(descriptor, index).asInt

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        getElementForIndex(descriptor, index).asLong

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        getElementForIndex(descriptor, index).asString

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        getElementForIndex(descriptor, index).asBoolean

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val value = element().asString
        return enumDescriptor.getElementIndex(value)
    }

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean = element !is JsonNull
    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? = null

    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this

}
