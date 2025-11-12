package io.github.skydynamic.maidataviewer.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.runtime.Stable
import io.github.skydynamic.maidataviewer.Application.Companion.application
import java.io.File

@Suppress("unused")
object BitmapExtension {
    fun Int.getBitmap() = application.getBitmapFromDrawable(this)

    @Stable
    inline val Int.drawableBitmap
        get() = getBitmap()

    fun File.getBitmap(): Bitmap? {
        return if (this.exists()) {
            BitmapFactory.decodeFile(this.absolutePath)
        } else {
            null
        }
    }

    fun ByteArray.getBitmap(): Bitmap {
        return BitmapFactory.decodeByteArray(this, 0, this.size)
    }

    fun Bitmap.scaleBitmap(width: Float, height: Float): Bitmap {
        val scaleWidth = (width) / this.width
        val scaleHeight = (height) / this.height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        val newBM = Bitmap.createBitmap(
            this, 0, 0,
            this.width, this.height, matrix, false
        )
        if (!this.isRecycled) {
            this.recycle()
        }
        return newBM
    }

    fun Bitmap.scaleBitmapByWidth(targetWidth: Float): Bitmap {
        val scale = targetWidth / this.width
        val targetHeight = (this.height * scale)
        return this.scaleBitmap(targetWidth, targetHeight)
    }

    fun Bitmap.scaleBitmapByHeight(targetHeight: Float): Bitmap {
        val scale = targetHeight.toFloat() / this.height
        val targetWidth = (this.width * scale)
        return this.scaleBitmap(targetWidth, targetHeight)
    }
}