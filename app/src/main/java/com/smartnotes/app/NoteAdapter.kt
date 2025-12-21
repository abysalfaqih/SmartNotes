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
        val accentBar: View = view.findViewById(R.id.accentBar)
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

        // Show/hide pin icon and accent bar
        if (note.isPinned) {
            holder.pinIcon.visibility = View.VISIBLE
            holder.accentBar.visibility = View.VISIBLE
        } else {
            holder.pinIcon.visibility = View.GONE
            holder.accentBar.visibility = View.GONE
        }

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

        // Long press handler with modern animation
        var longPressHandler: android.os.Handler? = null
        var isLongPressTriggered = false

        holder.itemView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    isLongPressTriggered = false
                    // Modern scale animation
                    v.animate()
                        .scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(150)
                        .start()

                    longPressHandler = android.os.Handler(android.os.Looper.getMainLooper())
                    longPressHandler?.postDelayed({
                        // Haptic feedback
                        v.performHapticFeedback(
                            android.view.HapticFeedbackConstants.LONG_PRESS,
                            android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                        )
                        isLongPressTriggered = true

                        // Call long click callback
                        onNoteLongClick(note)

                        // Reset scale with bounce
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

                    // Reset scale
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
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