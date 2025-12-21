package com.smartnotes.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartnotes.app.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private var notes: List<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    var isSelectionMode = false
        set(value) {
            field = value
            if (!value) {
                notes.forEach { it.isSelected = false }
            }
            notifyDataSetChanged()
        }

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: com.google.android.material.card.MaterialCardView = view.findViewById(R.id.cardView)
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val contentTextView: TextView = view.findViewById(R.id.contentTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
        val categoryTextView: TextView = view.findViewById(R.id.categoryTextView)
        val pinIcon: TextView = view.findViewById(R.id.pinIcon)
        val selectCheckbox: CheckBox = view.findViewById(R.id.selectCheckbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content
        holder.timestampTextView.text = formatDate(note.timestamp)
        holder.categoryTextView.text = note.category

        // Set card color
        try {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor(note.color))
        } catch (e: Exception) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.WHITE)
        }

        // Show/hide pin icon
        holder.pinIcon.visibility = if (note.isPinned) View.VISIBLE else View.GONE

        holder.selectCheckbox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.selectCheckbox.isChecked = note.isSelected

        holder.selectCheckbox.setOnCheckedChangeListener { _, isChecked ->
            note.isSelected = isChecked
        }

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                note.isSelected = !note.isSelected
                holder.selectCheckbox.isChecked = note.isSelected
            } else {
                onNoteClick(note)
            }
        }

        // Long press handler
        var longPressHandler: android.os.Handler? = null
        var isLongPressTriggered = false

        holder.itemView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    isLongPressTriggered = false
                    // Add visual feedback - scale down animation
                    v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .start()

                    longPressHandler = android.os.Handler(android.os.Looper.getMainLooper())
                    longPressHandler?.postDelayed({
                        // Haptic feedback
                        v.performHapticFeedback(
                            android.view.HapticFeedbackConstants.LONG_PRESS,
                            android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                        )
                        isLongPressTriggered = true

                        // Call long click callback - show menu instead of selection mode
                        onNoteLongClick(note)

                        // Reset scale
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }, 500) // 500ms delay
                    false
                }
                android.view.MotionEvent.ACTION_UP -> {
                    longPressHandler?.removeCallbacksAndMessages(null)

                    // Reset scale
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    // If not long press, trigger normal click
                    if (!isLongPressTriggered) {
                        v.performClick()
                    }
                    false
                }
                android.view.MotionEvent.ACTION_CANCEL -> {
                    longPressHandler?.removeCallbacksAndMessages(null)
                    isLongPressTriggered = false

                    // Reset scale
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    false
                }
                else -> false
            }
        }

        holder.itemView.setOnLongClickListener {
            true // Consume the event to prevent default behavior
        }
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    fun getSelectedNotes(): List<Note> {
        return notes.filter { it.isSelected }
    }

    fun selectAll() {
        notes.forEach { it.isSelected = true }
        notifyDataSetChanged()
    }

    fun deselectAll() {
        notes.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }
}