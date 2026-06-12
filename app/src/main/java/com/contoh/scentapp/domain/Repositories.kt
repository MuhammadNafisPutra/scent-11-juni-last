package com.contoh.scentapp.domain

import com.contoh.scentapp.data.model.CartItem
import com.contoh.scentapp.data.model.Order
import com.contoh.scentapp.data.model.OrderStatus
import com.contoh.scentapp.data.model.Parfum
import com.contoh.scentapp.data.model.ParfumFilter
import com.contoh.scentapp.data.model.Review
import com.contoh.scentapp.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val isLoggedIn: Boolean

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, fullName: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> // ✅ BARU
}

interface ProductRepository {
    fun getParfumList(): Flow<List<Parfum>>
    fun searchParfum(query: String, filters: ParfumFilter): Flow<List<Parfum>>
    suspend fun getParfumById(id: String): Parfum?
    suspend fun addParfum(parfum: Parfum): Result<String>
    suspend fun updateParfum(parfum: Parfum): Result<Unit>
    suspend fun deleteParfum(id: String): Result<Unit>
    fun getSellerParfums(sellerId: String): Flow<List<Parfum>>
    fun getReviews(parfumId: String): Flow<List<Review>>
    suspend fun addReview(review: Review): Result<Unit>
}

interface CartRepository {
    fun getCartItems(userId: String): Flow<List<CartItem>>
    suspend fun addToCart(userId: String, item: CartItem): Result<Unit>
    suspend fun updateQuantity(userId: String, itemId: String, quantity: Int): Result<Unit>
    suspend fun removeFromCart(userId: String, itemId: String): Result<Unit>
    suspend fun clearCart(userId: String): Result<Unit>
}

interface OrderRepository {
    fun getBuyerOrders(buyerId: String): Flow<List<Order>>
    fun getSellerOrders(sellerId: String): Flow<List<Order>>
    suspend fun createOrder(order: Order): Result<String>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit>
}

interface UserRepository {
    suspend fun getUserById(uid: String): User?
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun updateScentProfile(uid: String, scentProfile: List<String>): Result<Unit>
}