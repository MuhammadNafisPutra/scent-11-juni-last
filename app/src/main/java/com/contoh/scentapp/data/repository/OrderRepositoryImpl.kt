package com.contoh.scentapp.data.repository

import com.contoh.scentapp.domain.model.Order
import com.contoh.scentapp.domain.model.OrderStatus
import com.contoh.scentapp.domain.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementasi [OrderRepository] berbasis Firestore.
 *
 * Setelah pembayaran dikonfirmasi (COD maupun Transfer), [createOrder] dipanggil
 * untuk menyimpan dokumen pesanan ke koleksi "orders" agar muncul di
 * Riwayat Pesanan (buyer) dan Pesanan Masuk (seller).
 *
 * Catatan: query di sini sengaja TIDAK menggunakan `.orderBy()` yang
 * dikombinasikan dengan `.whereEqualTo()`, karena kombinasi tersebut
 * membutuhkan composite index di Firestore. Tanpa index tersebut,
 * `addSnapshotListener` akan langsung error (FAILED_PRECONDITION) dan
 * listener berhenti permanen -> daftar tidak pernah ter-update lagi
 * meski ada data baru. Sebagai gantinya, hasil diurutkan secara manual
 * di klien berdasarkan `createdAt` (descending).
 */
class OrderRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : OrderRepository {

    companion object {
        private const val COLLECTION = "orders"
    }

    val currentUserId: String? get() = auth.currentUser?.uid

    override suspend fun createOrder(order: Order): Result<String> {
        return try {
            val buyerId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User belum login"))

            val docRef = firestore.collection(COLLECTION).document()
            val newOrder = order.copy(
                id        = docRef.id,
                buyerId   = buyerId,
                createdAt = if (order.createdAt > 0L) order.createdAt else System.currentTimeMillis()
            )
            docRef.set(newOrder).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getBuyerOrders(buyerId: String): Flow<List<Order>> = callbackFlow {
        if (buyerId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection(COLLECTION)
            .whereEqualTo("buyerId", buyerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(Order::class.java) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override fun getSellerOrders(sellerId: String): Flow<List<Order>> = callbackFlow {
        if (sellerId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection(COLLECTION)
            .whereEqualTo("sellerId", sellerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(Order::class.java) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            firestore.collection(COLLECTION)
                .document(orderId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Ambil satu pesanan berdasarkan ID, dipakai oleh halaman detail pesanan. */
    suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val doc = firestore.collection(COLLECTION).document(orderId).get().await()
            val order = doc.toObject(Order::class.java)
                ?: return Result.failure(Exception("Pesanan tidak ditemukan"))
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
