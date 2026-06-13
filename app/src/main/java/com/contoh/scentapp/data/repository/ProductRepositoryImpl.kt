package com.contoh.scentapp.data.repository

import android.content.Context
import android.net.Uri
import com.contoh.scentapp.domain.model.Parfum
import com.contoh.scentapp.data.remote.CloudinaryUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepositoryImpl(
    private val firestore : FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth      : FirebaseAuth      = FirebaseAuth.getInstance()
) {
    companion object {
        private const val COLLECTION = "parfums"
    }

    // â”€â”€ Upload gambar ke Cloudinary â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Upload gambar produk ke Cloudinary.
     * Return secure_url yang akan disimpan ke field imageUrl di Firestore.
     */
    suspend fun uploadProductImage(context: Context, imageUri: Uri): Result<String> {
        return CloudinaryUploader.upload(context, imageUri)
    }

    // â”€â”€ Tambah produk baru ke Firestore â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    suspend fun addParfum(parfum: Parfum): Result<Unit> {
        return try {
            val sellerId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User belum login"))

            val docRef  = firestore.collection(COLLECTION).document()
            val newData = parfum.copy(
                id        = docRef.id,
                sellerId  = sellerId,
                createdAt = System.currentTimeMillis()
            )
            docRef.set(newData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // â”€â”€ Ambil semua produk (realtime) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun getAllParfums(): Flow<List<Parfum>> = callbackFlow {
        val listener = firestore.collection(COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(Parfum::class.java) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // â”€â”€ Ambil produk milik seller yang sedang login (realtime) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun getMyParfums(): Flow<List<Parfum>> = callbackFlow {
        val sellerId = auth.currentUser?.uid
        if (sellerId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        // Catatan: TIDAK menggunakan .orderBy("createdAt") di sini.
        // whereEqualTo("sellerId", ...) + orderBy("createdAt") membutuhkan
        // composite index di Firestore. Jika index belum dibuat, listener
        // akan langsung error (FAILED_PRECONDITION) dan berhenti permanen,
        // sehingga produk baru tidak akan pernah muncul tanpa restart app.
        // Sebagai gantinya, urutkan hasilnya di klien.
        val listener = firestore.collection(COLLECTION)
            .whereEqualTo("sellerId", sellerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(Parfum::class.java) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // â”€â”€ Update produk â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    suspend fun updateParfum(parfum: Parfum): Result<Unit> {
        return try {
            firestore.collection(COLLECTION)
                .document(parfum.id)
                .set(parfum)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // â”€â”€ Hapus produk â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    suspend fun deleteParfum(parfumId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION)
                .document(parfumId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // â”€â”€ Ambil satu produk by ID â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    suspend fun getParfumById(parfumId: String): Result<Parfum> {
        return try {
            val doc = firestore.collection(COLLECTION)
                .document(parfumId)
                .get()
                .await()
            val parfum = doc.toObject(Parfum::class.java)
                ?: return Result.failure(Exception("Produk tidak ditemukan"))
            Result.success(parfum)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}