package kotlinx.serialization.gson

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

class GsonEncoder(var element: JsonElement? = null) : Encoder, CompositeEncoder {

    override val serializersModule: SerializersModule = SerializersModule {}
    private val jsonObject: JsonObject? get() = element?.asJsonObjectOrNull()
    private val jsonArray: JsonArray? get() = element?.asJsonArrayOrNull()
    private var currentIndex = 0
    private var currentKey: String? = null
    private fun JsonElement?.asJsonObjectOrNull(): JsonObject? = this?.takeIf { it.isJsonObject }?.asJsonObject
    private fun JsonElement?.asJsonArrayOrNull(): JsonArray? = this?.takeIf { it.isJsonArray }?.asJsonArray

    // ------------------- Encoder for primitive values -------------------
    override fun encodeBoolean(value: Boolean) = putValue(JsonPrimitive(value))
    override fun encodeByte(value: Byte) = putValue(JsonPrimitive(value))
    override fun encodeShort(value: Short) = putValue(JsonPrimitive(value))
    override fun encodeInt(value: Int) = putValue(JsonPrimitive(value))
    override fun encodeLong(value: Long) = putValue(JsonPrimitive(value))
    override fun encodeFloat(value: Float) = putValue(JsonPrimitive(value))
    override fun encodeDouble(value: Double) = putValue(JsonPrimitive(value))
    override fun encodeChar(value: Char) = putValue(JsonPrimitive(value.toString()))
    override fun encodeString(value: String) {
        if (jsonObject != null && currentKey == null) {
            currentKey = value
        } else {
            putValue(JsonPrimitive(value))
        }
    }

    private fun putValue(value: JsonElement) {
        val array = jsonArray
        val obj = jsonObject
        if (obj != null) {
            val key = currentKey
            if (key != null) {
                obj.add(key, value)
                currentKey = null
            } else {
                // For maps, the key itself might be what we are encoding right now
                if (value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                    currentKey = value.asString
                } else {
                    throw IllegalStateException("No key to encode value: $value")
                }
            }
        } else if (array != null) {
            array.add(value)
        } else {
            element = value
        }
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        putValue(JsonNull.INSTANCE)
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

    // ------------------- CompositeEncoder -------------------
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val newElement = when (descriptor.kind) {
            StructureKind.LIST -> {
                JsonArray()
            }
            else -> {
                JsonObject()
            }
        }
        if (element == null) {
            element = newElement
        }
        return GsonEncoder(newElement)
    }

    override fun endStructure(descriptor: SerialDescriptor) {}

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        currentIndex = index
        addElement(descriptor, index, JsonPrimitive(value))
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        currentIndex = index
        addElement(descriptor, index, JsonPrimitive(value))
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        currentIndex = index
        addElement(descriptor, index, JsonPrimitive(value))
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        currentIndex = index
        addElement(descriptor, index, JsonPrimitive(value.toString()))
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        currentIndex = index
        addElement(descriptor, index, JsonPrimitive(value))
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        currentIndex = index
        addElement(descriptor, index, JsonPrimitive(value))
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        currentIndex = index
        addElement(descriptor, index, JsonPrimitive(value))
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        currentIndex = index
        addElement(descriptor, index, JsonPrimitive(value))
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        currentIndex = index
        addElement(descriptor, index, JsonPrimitive(value))
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        currentIndex = index
        return this
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        currentIndex = index
        val childEncoder = GsonEncoder()
        serializer.serialize(childEncoder, value)
        childEncoder.element?.let { addElement(descriptor, index, it) }
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value == null) addElement(descriptor, index, JsonNull.INSTANCE)
        else encodeSerializableElement(descriptor, index, serializer, value)
    }

    private fun addElement(descriptor: SerialDescriptor, index: Int, value: JsonElement) {
        val obj = jsonObject
        val array = jsonArray
        if (obj != null) {
            if (descriptor.kind == StructureKind.MAP) {
                putValue(value)
            } else {
                val key = descriptor.getElementName(index)
                obj.add(key, value)
            }
        } else array?.add(value)
    }

}
