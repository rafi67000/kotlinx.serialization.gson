package kotlinx.serialization.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule

object Gson : StringFormat {
    override val serializersModule: SerializersModule = SerializersModule {}
    var gson: Gson = GsonBuilder().serializeNulls().create()

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        val encoder = GsonEncoder()
        serializer.serialize(encoder, value)
        return gson.toJson(encoder.element)
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        val jsonElement = JsonParser.parseString(string)
        val decoder = GsonDecoder(jsonElement)
        return deserializer.deserialize(decoder)
    }

}
