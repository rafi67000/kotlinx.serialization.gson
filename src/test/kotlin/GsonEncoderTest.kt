import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.gson.Gson
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GsonEncoderTest {

    @Serializable
    data class Simple(val name: String)

    @Serializable
    data class Nested(val simple: Simple)

    @Test
    fun testPrimitiveEncoding() {
        val result = Gson.encodeToString("test")
        assertEquals("\"test\"", result)
    }

    @Test
    fun testNestedEncoding() {
        val nested = Nested(Simple("test"))
        val result = Gson.encodeToString(nested)
        assertEquals("""{"simple":{"name":"test"}}""", result)
    }

    @Serializable
    data class MapData(val map: Map<String, Int>)

    @Test
    fun testMapEncoding() {
        val data = MapData(mapOf("a" to 1, "b" to 2))
        val result = Gson.encodeToString(data)
        assertEquals("""{"map":{"a":1,"b":2}}""", result)
    }

    @Test
    fun testTopLevelListEncoding() {
        val data = listOf("a", "b")
        val result = Gson.encodeToString(data)
        assertEquals("""["a","b"]""", result)
    }

    @Serializable
    data class NullableData(val name: String?)

    @Test
    fun testNullableEncoding() {
        val data = NullableData(null)
        val result = Gson.encodeToString(data)
        assertEquals("""{"name":null}""", result)
    }
}
