package com.example.gooutside.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class PhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "PhotoRepository"
    }

    private val resolver = context.contentResolver

    suspend fun saveToMediaStore(bitmap: Bitmap): PhotoSaveResult {
        return withContext(Dispatchers.IO) {
            val mediaCollection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val filename = "IMG_${System.currentTimeMillis()}"

            val imageDetails = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "GoOutside"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri =
                resolver.insert(mediaCollection, imageDetails)
                    ?: return@withContext PhotoSaveResult.Failure(
                        Exception("Failed to create new MediaStore record")
                    )

            try {
                resolver.openOutputStream(uri, "w")?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                Log.e(TAG, "Saving photo to MediaStore failed: ${e.message}", e)
            }

            imageDetails.clear()
            imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, imageDetails, null, null)

            return@withContext PhotoSaveResult.Success(uri.toString())
        }
    }

    suspend fun deleteFromMediaStore(imagePath: String) {}
}

sealed class PhotoSaveResult {
    data class Success(val uri: String) : PhotoSaveResult()
    data class Failure(val exception: Exception) : PhotoSaveResult()
}