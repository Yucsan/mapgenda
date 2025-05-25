package com.yucsan.mapgendafernandochang2025.util

import android.content.Context
import android.net.Uri
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryUploader {

    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            val config = hashMapOf(
                "cloud_name" to "dknlc31bw",
                "upload_preset" to "mapgenda1", // 👈 reemplaza con el tuyo real
                "folder" to "usuarios"                 // 👈 sube todo a /usuarios
            )
            MediaManager.init(context, config)
            initialized = true
        }
    }

    suspend fun subirImagenDesdeUri(context: Context, uri: Uri): String? {
        init(context)

        return suspendCancellableCoroutine { continuation ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("img_", ".jpg", context.cacheDir)
                tempFile.outputStream().use { fileOut ->
                    inputStream?.copyTo(fileOut)
                }

                MediaManager.get().upload(tempFile.path)
                    .unsigned("mapgenda1") // 👈 reemplaza con el nombre real
                    .option("folder", "usuarios")
                    .option("public_id", UUID.randomUUID().toString())
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {}

                        override fun onProgress(
                            requestId: String?,
                            bytes: Long,
                            totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(
                            requestId: String?,
                            resultData: MutableMap<Any?, Any?>?
                        ) {
                            val url = resultData?.get("secure_url") as? String
                            continuation.resume(url)
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            continuation.resumeWithException(Exception("Error al subir imagen: ${error?.description}"))
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            continuation.resumeWithException(Exception("Reintento al subir imagen: ${error?.description}"))
                        }
                    })
                    .dispatch()

            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
}
