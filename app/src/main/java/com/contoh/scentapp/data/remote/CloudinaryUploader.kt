package com.contoh.scentapp.data.remote

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object CloudinaryUploader {

    private const val CLOUD_NAME   = "dchjojabq"
    private const val UPLOAD_PRESET = "scentapp_unsigned"
    private const val UPLOAD_URL   = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Upload gambar ke Cloudinary via unsigned multipart POST.
     * Harus dipanggil dari coroutine (Dispatchers.IO).
     *
     * @param context  Context Android untuk membaca Uri
     * @param imageUri Uri gambar dari galeri atau kamera
     * @return         secure_url gambar yang tersimpan di Cloudinary
     */
    suspend fun upload(context: Context, imageUri: Uri): Result<String> {
        return try {
            // Baca bytes dari Uri
            val bytes = context.contentResolver
                .openInputStream(imageUri)
                ?.use { it.readBytes() }
                ?: return Result.failure(Exception("Tidak bisa membaca file gambar"))

            // Tentukan media type (default jpeg)
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"

            // Buat multipart request body
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    name     = "file",
                    filename = "upload.jpg",
                    body     = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                )
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
                ?: return Result.failure(Exception("Response kosong dari Cloudinary"))

            if (!response.isSuccessful) {
                return Result.failure(Exception("Upload gagal: HTTP ${response.code} — $responseBody"))
            }

            val json = JSONObject(responseBody)
            val secureUrl = json.optString("secure_url")

            if (secureUrl.isNullOrBlank()) {
                Result.failure(Exception("secure_url tidak ditemukan di response Cloudinary"))
            } else {
                Result.success(secureUrl)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}