package com.example.usermanagementeyetec.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    fun saveImageToInternalStorage(context: Context, uri: Uri, userId: Long): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val filename = "user_$userId.jpg"
            val dir = File(context.filesDir, "images")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteImageFromInternalStorage(context: Context, imagePath: String?) {
        imagePath?.let {
            try {
                File(it).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}