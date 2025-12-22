package com.smartnotes.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class RichEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    fun applyBold() {
        applyStyle(Typeface.BOLD)
    }

    fun applyItalic() {
        applyStyle(Typeface.ITALIC)
    }

    fun applyUnderline() {
        val start = selectionStart
        val end = selectionEnd

        if (start >= 0 && end > start) {
            val editable = text ?: return
            val spannable = if (editable is SpannableStringBuilder) {
                editable
            } else {
                SpannableStringBuilder(editable)
            }

            val spans = spannable.getSpans(start, end, UnderlineSpan::class.java)
            if (spans.isEmpty()) {
                spannable.setSpan(
                    UnderlineSpan(),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                spans.forEach { spannable.removeSpan(it) }
            }

            setText(spannable)
            setSelection(start, end)
        }
    }

    fun applyStrikethrough() {
        val start = selectionStart
        val end = selectionEnd

        if (start >= 0 && end > start) {
            val editable = text ?: return
            val spannable = if (editable is SpannableStringBuilder) {
                editable
            } else {
                SpannableStringBuilder(editable)
            }

            val spans = spannable.getSpans(start, end, StrikethroughSpan::class.java)
            if (spans.isEmpty()) {
                spannable.setSpan(
                    StrikethroughSpan(),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                spans.forEach { spannable.removeSpan(it) }
            }

            setText(spannable)
            setSelection(start, end)
        }
    }

    fun addBulletPoint() {
        val cursorPosition = selectionStart
        if (cursorPosition < 0) return

        val currentText = text.toString()
        val bullet = "• "

        val newText = when {
            cursorPosition == 0 -> bullet + currentText
            currentText.getOrNull(cursorPosition - 1) == '\n' -> {
                StringBuilder(currentText).insert(cursorPosition, bullet).toString()
            }
            else -> {
                StringBuilder(currentText).insert(cursorPosition, "\n$bullet").toString()
            }
        }

        setText(newText)

        val newPosition = when {
            cursorPosition == 0 -> bullet.length
            currentText.getOrNull(cursorPosition - 1) == '\n' -> cursorPosition + bullet.length
            else -> cursorPosition + bullet.length + 1
        }
        setSelection(newPosition)
    }

    // NEW: Apply text color
    fun applyTextColor(colorHex: String) {
        val start = selectionStart
        val end = selectionEnd

        if (start >= 0 && end > start) {
            val editable = text ?: return
            val spannable = if (editable is SpannableStringBuilder) {
                editable
            } else {
                SpannableStringBuilder(editable)
            }

            TextSpanHelper.applyTextColor(spannable, start, end, colorHex)

            setText(spannable)
            setSelection(start, end)
        }
    }

    // NEW: Apply highlight color
    fun applyHighlight(colorHex: String) {
        val start = selectionStart
        val end = selectionEnd

        if (start >= 0 && end > start) {
            val editable = text ?: return
            val spannable = if (editable is SpannableStringBuilder) {
                editable
            } else {
                SpannableStringBuilder(editable)
            }

            TextSpanHelper.applyHighlightColor(spannable, start, end, colorHex)

            setText(spannable)
            setSelection(start, end)
        }
    }

    // NEW: Apply font size
    fun applyFontSize(multiplier: Float) {
        val start = selectionStart
        val end = selectionEnd

        if (start >= 0 && end > start) {
            val editable = text ?: return
            val spannable = if (editable is SpannableStringBuilder) {
                editable
            } else {
                SpannableStringBuilder(editable)
            }

            TextSpanHelper.applyFontSize(spannable, start, end, multiplier)

            setText(spannable)
            setSelection(start, end)
        }
    }

    // NEW: Remove all formatting from selection
    fun clearFormatting() {
        val start = selectionStart
        val end = selectionEnd

        if (start >= 0 && end > start) {
            val editable = text ?: return
            val spannable = if (editable is SpannableStringBuilder) {
                editable
            } else {
                SpannableStringBuilder(editable)
            }

            TextSpanHelper.removeAllFormatting(spannable, start, end)

            setText(spannable)
            setSelection(start, end)
        }
    }

    // NEW: Insert quote block
    fun insertQuote() {
        val cursorPosition = selectionStart
        if (cursorPosition < 0) return

        val currentText = text.toString()
        val quote = "❝ "

        val newText = when {
            cursorPosition == 0 -> quote + currentText
            currentText.getOrNull(cursorPosition - 1) == '\n' -> {
                StringBuilder(currentText).insert(cursorPosition, quote).toString()
            }
            else -> {
                StringBuilder(currentText).insert(cursorPosition, "\n$quote").toString()
            }
        }

        setText(newText)

        val newPosition = when {
            cursorPosition == 0 -> quote.length
            currentText.getOrNull(cursorPosition - 1) == '\n' -> cursorPosition + quote.length
            else -> cursorPosition + quote.length + 1
        }
        setSelection(newPosition)
    }

    // NEW: Insert code block marker
    fun insertCodeBlock() {
        val cursorPosition = selectionStart
        if (cursorPosition < 0) return

        val currentText = text.toString()
        val code = "⟨ code ⟩\n\n"

        val newText = when {
            cursorPosition == 0 -> code + currentText
            currentText.getOrNull(cursorPosition - 1) == '\n' -> {
                StringBuilder(currentText).insert(cursorPosition, code).toString()
            }
            else -> {
                StringBuilder(currentText).insert(cursorPosition, "\n$code").toString()
            }
        }

        setText(newText)

        val newPosition = when {
            cursorPosition == 0 -> 8  // Position between markers
            currentText.getOrNull(cursorPosition - 1) == '\n' -> cursorPosition + 8
            else -> cursorPosition + 9
        }
        setSelection(newPosition)
    }

    private fun applyStyle(style: Int) {
        val start = selectionStart
        val end = selectionEnd

        if (start >= 0 && end > start) {
            val editable = text ?: return
            val spannable = if (editable is SpannableStringBuilder) {
                editable
            } else {
                SpannableStringBuilder(editable)
            }

            val existingSpans = spannable.getSpans(start, end, StyleSpan::class.java)
            val hasStyle = existingSpans.any { it.style == style }

            if (!hasStyle) {
                spannable.setSpan(
                    StyleSpan(style),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                existingSpans.filter { it.style == style }.forEach {
                    spannable.removeSpan(it)
                }
            }

            setText(spannable)
            setSelection(start, end)
        }
    }

    fun getCurrentFormattedText(): CharSequence {
        return text ?: ""
    }

    fun setFormattedText(formattedText: CharSequence) {
        try {
            setText(formattedText)
        } catch (e: Exception) {
            e.printStackTrace()
            setText(formattedText.toString())
        }
    }

    // NEW: Check if current selection has formatting
    fun hasFormatting(): Boolean {
        val start = selectionStart
        val end = selectionEnd

        if (start >= 0 && end > start) {
            val editable = text ?: return false
            val spannable = if (editable is SpannableStringBuilder) {
                editable
            } else {
                return false
            }

            return TextSpanHelper.hasFormatting(spannable, start, end)
        }
        return false
    }
}