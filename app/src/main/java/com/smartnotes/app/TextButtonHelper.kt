package com.smartnotes.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageButton

object TextButtonHelper {

    fun setTextAsIcon(button: ImageButton, text: String, textSize: Float = 40f, isBold: Boolean = false) {
        val paint = Paint().apply {
            this.textSize = textSize
            this.color = Color.BLACK
            this.textAlign = Paint.Align.CENTER
            this.typeface = if (isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            isAntiAlias = true
        }

        val width = paint.measureText(text).toInt() + 20
        val height = (textSize + 20).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val x = width / 2f
        val y = height / 2f - (paint.descent() + paint.ascent()) / 2f

        canvas.drawText(text, x, y, paint)

        button.setImageDrawable(BitmapDrawable(button.context.resources, bitmap))
    }
}