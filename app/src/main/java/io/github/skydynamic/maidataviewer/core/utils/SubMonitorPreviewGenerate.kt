package io.github.skydynamic.maidataviewer.core.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.graphics.createBitmap
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiTitleData
import io.github.skydynamic.maidataviewer.core.manager.resource.ResourceManagerType
import io.github.skydynamic.maidataviewer.core.utils.BitmapExtension.drawableBitmap
import io.github.skydynamic.maidataviewer.core.utils.BitmapExtension.getBitmap
import io.github.skydynamic.maidataviewer.core.utils.BitmapExtension.scaleBitmapByWidth
import io.github.skydynamic.maidataviewer.core.utils.CanvasExtension.drawImage
import io.github.skydynamic.maidataviewer.core.utils.CanvasExtension.drawText
import java.io.File

class SubMonitorPreviewGenerate(
    val playerName: String,
    val icon: File? = null,
    val namePlate: File? = null,
    val frame: File? = null,
    val title: MaimaiTitleData,
    val showCharaGroup: Boolean = true,
) {
    val defaultIcon = ResourceManagerType.ICON.instance!!.getResByteFromAssets(0)!!
    val defaultPlate = ResourceManagerType.PLATE.instance!!.getResByteFromAssets(0)!!
    val defaultFrame = ResourceManagerType.FRAME.instance!!.getResByteFromAssets(0)!!

    fun generate(): Bitmap {
        val bgBitmap = R.drawable.background.drawableBitmap

        val zoomFactor = bgBitmap.width / 1440f

        val charaBitmap = R.drawable.charagroup.drawableBitmap
        var iconBitmap = icon?.getBitmap() ?: defaultIcon.getBitmap()
        var plateBitmap = namePlate?.getBitmap() ?: defaultPlate.getBitmap()
        var frameBitmap = frame?.getBitmap() ?: defaultFrame.getBitmap()
        var userNameArea = R.drawable.playername.drawableBitmap
        var titleBitmap = title.rareType.resId.drawableBitmap

        iconBitmap = iconBitmap.scaleBitmapByWidth(140f * zoomFactor)
        plateBitmap = plateBitmap.scaleBitmapByWidth(960f * zoomFactor)
        frameBitmap = frameBitmap.scaleBitmapByWidth(bgBitmap.width.toFloat())
        userNameArea = userNameArea.scaleBitmapByWidth(365f * zoomFactor)
        titleBitmap = titleBitmap.scaleBitmapByWidth(350f * zoomFactor)

        val resultBitmap = createBitmap(
            bgBitmap.width,
            bgBitmap.height,
            bgBitmap.config ?: Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(resultBitmap)

        canvas.drawImage(bgBitmap, 0f, 0f)
        canvas.drawImage(frameBitmap, 0f, 0f)
        canvas.drawImage(plateBitmap, 43f * zoomFactor, 34f * zoomFactor)
        canvas.drawImage(iconBitmap, 50f * zoomFactor, 42f * zoomFactor)
        canvas.drawImage(userNameArea, 190f * zoomFactor, 85f * zoomFactor)
        canvas.drawText(
            text = playerName,
            textSize = 30f * zoomFactor,
            x = 210f * zoomFactor,
            y = 130f * zoomFactor,
            font = R.font.liberation_sans,
            bold = true,
        )
        canvas.drawImage(titleBitmap, 195f * zoomFactor, 138f * zoomFactor)
        canvas.drawText(
            text = title.name ?: "",
            textSize = 20f * zoomFactor,
            x = 195f * zoomFactor + titleBitmap.width.toFloat() / 2f,
            y = 167f * zoomFactor,
            color = Color.WHITE,
            outline = TextOutline(Color.BLACK, 8f),
            align = TextAlign.Center
        )
        if (showCharaGroup) {
            canvas.drawBitmap(charaBitmap, 0f, 0f,  null)
        }

        return resultBitmap
    }
}