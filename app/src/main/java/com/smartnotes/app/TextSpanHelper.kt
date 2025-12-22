package com.smartnotes.app

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan

object TextSpanHelper {

    /**
     * Apply text color to selected text
     */
    fun applyTextColor(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int,
        colorHex: String
    ): SpannableStringBuilder {
        try {
            val color = Color.parseColor(colorHex)

            // Remove existing color spans
            val existingSpans = spannable.getSpans(start, end, ForegroundColorSpan::class.java)
            existingSpans.forEach { spannable.removeSpan(it) }

            spannable.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return spannable
    }

    /**
     * Apply highlight/background color to selected text
     */
    fun applyHighlightColor(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int,
        colorHex: String
    ): SpannableStringBuilder {
        try {
            val color = Color.parseColor(colorHex)

            // Remove existing background spans
            val existingSpans = spannable.getSpans(start, end, BackgroundColorSpan::class.java)
            existingSpans.forEach { spannable.removeSpan(it) }

            spannable.setSpan(
                BackgroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return spannable
    }

    /**
     * Apply font size (relative to base size)
     * multiplier: 0.5 = 50%, 1.0 = 100%, 1.5 = 150%, etc.
     */
    fun applyFontSize(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int,
        multiplier: Float
    ): SpannableStringBuilder {
        try {
            // Remove existing size spans
            val existingSpans = spannable.getSpans(start, end, RelativeSizeSpan::class.java)
            existingSpans.forEach { spannable.removeSpan(it) }

            spannable.setSpan(
                RelativeSizeSpan(multiplier),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return spannable
    }

    /**
     * Remove all formatting from selected text
     */
    fun removeAllFormatting(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int
    ): SpannableStringBuilder {
        try {
            // Get all spans in range
            val spans = spannable.getSpans(start, end, Any::class.java)

            // Remove each span
            spans.forEach { span ->
                if (span is ForegroundColorSpan ||
                    span is BackgroundColorSpan ||
                    span is StyleSpan ||
                    span is RelativeSizeSpan ||
                    span is UnderlineSpan ||
                    span is StrikethroughSpan) {
                    spannable.removeSpan(span)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return spannable
    }

    /**
     * Check if text has any formatting
     */
    fun hasFormatting(spannable: SpannableStringBuilder, start: Int, end: Int): Boolean {
        val spans = spannable.getSpans(start, end, Any::class.java)
        return spans.any { span ->
            span is ForegroundColorSpan ||
                    span is BackgroundColorSpan ||
                    span is StyleSpan ||
                    span is RelativeSizeSpan ||
                    span is UnderlineSpan ||
                    span is StrikethroughSpan
        }
    }

    /**
     * Color presets for quick access
     */
    object ColorPresets {
        val TEXT_COLORS = arrayOf(
            "Hitam" to "#000000",
            "Merah" to "#E53935",
            "Hijau" to "#43A047",
            "Biru" to "#1E88E5",
            "Kuning" to "#FDD835",
            "Oranye" to "#FB8C00",
            "Ungu" to "#8E24AA",
            "Pink" to "#D81B60",
            "Abu-abu" to "#757575"
        )

        val HIGHLIGHT_COLORS = arrayOf(
            "Kuning Terang" to "#FFEB3B",
            "Hijau Terang" to "#C8E6C9",
            "Biru Terang" to "#B3E5FC",
            "Pink Terang" to "#F8BBD0",
            "Orange Terang" to "#FFE0B2",
            "Ungu Terang" to "#E1BEE7",
            "Merah Terang" to "#FFCDD2",
            "Abu-abu Terang" to "#E0E0E0"
        )
    }

    /**
     * Font size presets
     */
    object FontSizePresets {
        const val VERY_SMALL = 0.75f
        const val SMALL = 0.9f
        const val NORMAL = 1.0f
        const val LARGE = 1.25f
        const val VERY_LARGE = 1.5f
        const val HUGE = 2.0f

        val SIZES = arrayOf(
            "Sangat Kecil" to VERY_SMALL,
            "Kecil" to SMALL,
            "Normal" to NORMAL,
            "Besar" to LARGE,
            "Sangat Besar" to VERY_LARGE,
            "Raksasa" to HUGE
        )
    }
}