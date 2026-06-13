package com.contoh.scentapp.data.repository

import com.contoh.scentapp.domain.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Mengelola ulasan (review) produk di Firestore.
 *
 * Struktur dokumen:
 * parfums/{parfumId}/reviews/{reviewId} -> Review
 *
 * Setiap kali ulasan baru ditambahkan, statistik agregat pada dokumen
 * parfums/{parfumId} (reviewCount, avgLongevity, avgSillage, avgProjection)
 * juga ikut diperbarui â€” sehingga rating yang tampil di Home/Detail
 * selalu sinkron dengan ulasan yang benar-benar ditulis.
 */
class ReviewRepository(
    private val firestore : FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth      : FirebaseAuth      = FirebaseAuth.getInstance()
) {
    companion object {
        private const val PARFUM_COLLECTION = "parfums"
        private const val REVIEW_SUBCOLLECTION = "reviews"
    }

    private fun reviewsRef(parfumId: String) =
        firestore.collection(PARFUM_COLLECTION).document(parfumId).collection(REVIEW_SUBCOLLECTION)

    /**
     * Ambil semua ulasan untuk satu produk secara realtime, terbaru di atas.
     */
    fun getReviews(parfumId: String): Flow<List<Review>> = callbackFlow {
        if (parfumId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = reviewsRef(parfumId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(Review::class.java)?.copy(
                            id = doc.id.hashCode()
                        )
                    }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Tambah ulasan baru untuk produk [parfumId], lalu sinkronkan statistik
     * agregat (reviewCount & rata-rata metrik) pada dokumen produk terkait
     * sehingga muncul di halaman detail produk.
     */
    suspend fun addReview(parfumId: String, review: Review): Result<Unit> {
        return try {
            if (parfumId.isBlank()) {
                return Result.failure(Exception("Produk tidak ditemukan"))
            }

            val uid = auth.currentUser?.uid ?: ""
            val displayName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                ?: getCurrentUserName(uid)
                ?: "Pembeli"

            val finalReview = review.copy(
                parfumId   = parfumId,
                reviewerId = uid,
                name       = displayName,
                initials   = initialsFrom(displayName),
                badge      = "PEMBELI TERVERIFIKASI",
                createdAt  = System.currentTimeMillis()
            )

            val docRef = reviewsRef(parfumId).document()
            docRef.set(finalReview).await()

            updateParfumAggregate(parfumId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hitung ulang reviewCount & rata-rata metrik wewangian berdasarkan
     * seluruh ulasan yang ada, lalu simpan ke dokumen parfums/{parfumId}.
     */
    private suspend fun updateParfumAggregate(parfumId: String) {
        try {
            val snapshot = reviewsRef(parfumId).get().await()
            val reviews  = snapshot.documents.mapNotNull { it.toObject(Review::class.java) }
            if (reviews.isEmpty()) return

            val count       = reviews.size
            val avgLongevity  = reviews.map { it.longevity }.average().toFloat()
            val avgSillage    = reviews.map { it.sillage }.average().toFloat()
            val avgProjection = reviews.map { it.projection }.average().toFloat()

            firestore.collection(PARFUM_COLLECTION).document(parfumId)
                .update(
                    mapOf(
                        "reviewCount"   to count,
                        "avgLongevity"  to avgLongevity,
                        "avgSillage"    to avgSillage,
                        "avgProjection" to avgProjection
                    )
                ).await()
        } catch (_: Exception) {
            // Statistik agregat bersifat best-effort; gagal di sini tidak
            // membatalkan pengiriman ulasan itu sendiri.
        }
    }

    private suspend fun getCurrentUserName(uid: String): String? {
        if (uid.isBlank()) return null
        return try {
            firestore.collection("users").document(uid).get().await()
                .getString("fullName")
                ?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun initialsFrom(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "U"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else            -> "${parts.first().first()}${parts.last().first()}".uppercase()
        }
    }
}
