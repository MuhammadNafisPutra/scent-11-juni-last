package com.contoh.scentapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FavoriteRepository(
    private val firestore : FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth      : FirebaseAuth      = FirebaseAuth.getInstance()
) {
    companion object {
        private const val COLLECTION = "favorites"
    }

    // Dokumen struktur di Firestore:
    // favorites/{userId}/items/{parfumId}  →  { parfumId: String, addedAt: Long }

    private fun userFavoritesRef(userId: String) =
        firestore.collection(COLLECTION).document(userId).collection("items")

    /**
     * Observe daftar ID parfum yang difavoritkan user saat ini — realtime.
     * Return Flow<Set<String>> berisi parfumId.
     */
    fun getFavoriteIds(): Flow<Set<String>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }

        val listener = userFavoritesRef(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val ids = snapshot?.documents
                    ?.mapNotNull { it.getString("parfumId") }
                    ?.toSet()
                    ?: emptySet()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Tambah parfum ke favorit.
     */
    suspend fun addFavorite(parfumId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User belum login"))

            userFavoritesRef(userId)
                .document(parfumId)
                .set(mapOf("parfumId" to parfumId, "addedAt" to System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hapus parfum dari favorit.
     */
    suspend fun removeFavorite(parfumId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User belum login"))

            userFavoritesRef(userId)
                .document(parfumId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle favorit — tambah jika belum ada, hapus jika sudah ada.
     */
    suspend fun toggleFavorite(parfumId: String, currentlyFavorited: Boolean): Result<Unit> {
        return if (currentlyFavorited) removeFavorite(parfumId) else addFavorite(parfumId)
    }

    /**
     * Ambil semua parfum yang difavoritkan (sekali, bukan realtime).
     * Berguna untuk FavoriteScreen.
     */
    fun getFavoriteParfums(): Flow<List<String>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = userFavoritesRef(userId)
            .orderBy("addedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val ids = snapshot?.documents
                    ?.mapNotNull { it.getString("parfumId") }
                    ?: emptyList()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }
}