package com.contoh.scentapp.domain.usecase.profile

import android.app.Application
import android.net.Uri
import com.contoh.scentapp.data.remote.CloudinaryUploader
import com.contoh.scentapp.domain.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdateProfileUseCase(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) {
    suspend operator fun invoke(fullName: String, email: String, photoUri: Uri?, address: String? = null): Result<String?> {
        return try {
            val uid = authRepository.currentUserId ?: return Result.failure(Exception("User not found"))

            val imageUrl = if (photoUri != null) {
                withContext(Dispatchers.IO) {
                    CloudinaryUploader.upload(application, photoUri).getOrNull()
                }
            } else null

            val updates = mutableMapOf<String, Any>(
                "fullName" to fullName,
                "email"    to email
            )
            if (imageUrl != null) updates["profileImageUrl"] = imageUrl
            if (address != null) updates["defaultAddress"] = address

            firestore.collection("users").document(uid)
                .update(updates)
                .await()

            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
