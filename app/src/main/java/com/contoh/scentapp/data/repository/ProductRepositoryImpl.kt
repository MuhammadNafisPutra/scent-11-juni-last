package com.contoh.scentapp.data.repository

import android.content.Context
import android.net.Uri
import com.contoh.scentapp.data.model.Parfum
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

    // ── Upload gambar ke Cloudinary ──────────────────────────────────────────

    /**
     * Upload gambar produk ke Cloudinary.
     * Return secure_url yang akan disimpan ke field imageUrl di Firestore.
     */
    suspend fun uploadProductImage(context: Context, imageUri: Uri): Result<String> {
        return CloudinaryUploader.upload(context, imageUri)
    }

    // ── Tambah produk baru ke Firestore ──────────────────────────────────────

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

    // ── Ambil semua produk (realtime) ────────────────────────────────────────

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

    // ── Ambil produk milik seller yang sedang login (realtime) ───────────────

    fun getMyParfums(): Flow<List<Parfum>> = callbackFlow {
        val sellerId = auth.currentUser?.uid
        if (sellerId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection(COLLECTION)
            .whereEqualTo("sellerId", sellerId)
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

    // ── Update produk ────────────────────────────────────────────────────────

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

    // ── Hapus produk ─────────────────────────────────────────────────────────

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

    // ── Ambil satu produk by ID ──────────────────────────────────────────────

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