import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.gson.Gson
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GsonDecoderTest {

    val json = """
        {
          "user": {
            "id": 12345,
            "username": "coder_sam",
            "isActive": true,
            "roles": ["admin", "developer"],
            "profile": {
              "firstName": "Sam",
              "lastName": "Smith",
              "email": "sam.smith@example.com"
            }
          },
          "products": [
            { "productId": "A001", "name": "Laptop", "price": 1200.50 },
            { "productId": "B002", "name": "Mouse", "price": 25.00 }
          ],
          "lastLogin": null
        }
    """.trimIndent()

    @Test
    fun testDecodeApiResponse() {
        val deserialized = Gson.decodeFromString<ApiResponse>(json)
        assertNotNull(deserialized)
        assertEquals("coder_sam", deserialized.user.username)
        assertEquals(2, deserialized.products.size)
        assertEquals(null, deserialized.lastLogin)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testDecodeApiResponseMissingLastLogin() {
        val jsonNoLastLogin = """
        {
          "user": {
            "id": 12345,
            "username": "coder_sam",
            "isActive": true,
            "roles": ["admin", "developer"],
            "profile": {
              "firstName": "Sam",
              "lastName": "Smith",
              "email": "sam.smith@example.com"
            }
          },
          "products": [
            { "productId": "A001", "name": "Laptop", "price": 1200.50 },
            { "productId": "B002", "name": "Mouse", "price": 25.00 }
          ]
        }
        """.trimIndent()
        // This should throw MissingFieldException if it's required and missing
        assertFailsWith<MissingFieldException> {
            Gson.decodeFromString<ApiResponse>(jsonNoLastLogin)
        }
    }
}
