import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val user: User,
    val products: List<Product>,
    val lastLogin: String?
)

@Serializable
data class User(
    val id: Long,
    val username: String,
    val isActive: Boolean,
    val roles: List<String>,
    val profile: Profile
)

@Serializable
data class Profile(
    val firstName: String,
    val lastName: String,
    val email: String
)

@Serializable
data class Product(
    val productId: String,
    val name: String,
    val price: Double
)
