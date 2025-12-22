package com.smartnotes.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.smartnotes.app.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrashAdapter(
    private var notes: List<Note>,
    private val onRestoreClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit
) : RecyclerView.Adapter<TrashAdapter.TrashViewHolder>() {

    class TrashViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: com.google.android.material.card.MaterialCardView = view.findViewById(R.id.cardView)
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val contentTextView: TextView = view.findViewById(R.id.contentTextView)
        val deleteWarningTextView: TextView = view.findViewById(R.id.deleteWarningTextView)
        val restoreButton: MaterialButton = view.findViewById(R.id.restoreButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trash_note, parent, false)
        return TrashViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrashViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content

        // Show days until permanent deletion
        val daysRemaining = note.getDaysUntilPermanentDelete()
        holder.deleteWarningTextView.text = when {
            daysRemaining <= 0 -> "Dihapus permanen segera"
            daysRemaining == 1 -> "Dihapus permanen besok"
            else -> "Dihapus permanen dalam $daysRemaining hari"
        }

        // Set card color based on original note color
        try {
            val color = android.graphics.Color.parseColor(note.color)
            // Apply slight transparency to indicate deleted state
            val alphaColor = android.graphics.Color.argb(
                180,
                android.graphics.Color.red(color),
                android.graphics.Color.green(color),
                android.graphics.Color.blue(color)
            )
            holder.cardView.setCardBackgroundColor(alphaColor)
        } catch (e: Exception) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
        }

        holder.restoreButton.setOnClickListener {
            // Button animation
            it.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()

            onRestoreClick(note)
        }

        // Long press for permanent delete
        var longPressHandler: android.os.Handler? = null
        var isLongPressTriggered = false

        holder.itemView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    isLongPressTriggered = false
                    v.animate()
                        .scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(150)
                        .start()

                    longPressHandler = android.os.Handler(android.os.Looper.getMainLooper())
                    longPressHandler?.postDelayed({
                        v.performHapticFeedback(
                            android.view.HapticFeedbackConstants.LONG_PRESS,
                            android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                        )
                        isLongPressTriggered = true
                        onNoteLongClick(note)

                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start()
                    }, 500)
                    false
                }
                android.view.MotionEvent.ACTION_UP -> {
                    longPressHandler?.removeCallbacksAndMessages(null)
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                    false
                }
                android.view.MotionEvent.ACTION_CANCEL -> {
                    longPressHandler?.removeCallbacksAndMessages(null)
                    isLongPressTriggered = false
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                    false
                }
                else -> false
            }
        }

        holder.itemView.setOnLongClickListener {
            true
        }
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}