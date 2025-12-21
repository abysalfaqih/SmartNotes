package com.smartnotes.app

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartnotes.app.data.Note
import com.smartnotes.app.data.NoteDaoImpl
import com.smartnotes.app.data.NoteRepository
import kotlinx.coroutines.launch

class AddNoteActivity : AppCompatActivity() {

    private lateinit var repository: NoteRepository
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: RichEditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var editButton: Button
    private lateinit var formattingToolbar: HorizontalScrollView
    private lateinit var categoryButton: TextView
    private lateinit var colorButton: TextView

    private lateinit var btnCheckList: ImageButton
    private lateinit var btnH1: ImageButton
    private lateinit var btnH2: ImageButton
    private lateinit var btnH3: ImageButton
    private lateinit var btnBold: ImageButton
    private lateinit var btnClose: ImageButton

    private var existingNote: Note? = null
    private var isEditMode = false
    private var selectedCategory = "Semua"
    private var selectedColor = "#FFFFFF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val noteDao = NoteDaoImpl(this)
        repository = NoteRepository(noteDao)

        initializeViews()
        setupToolbarButtons()
        loadExistingNote()
        setupButtonListeners()

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
        editButton = findViewById(R.id.editButton)
        formattingToolbar = findViewById(R.id.formattingToolbar)
        categoryButton = findViewById(R.id.categoryButton)
        colorButton = findViewById(R.id.colorButton)

        btnCheckList = findViewById(R.id.btnCheckList)
        btnH1 = findViewById(R.id.btnH1)
        btnH2 = findViewById(R.id.btnH2)
        btnH3 = findViewById(R.id.btnH3)
        btnBold = findViewById(R.id.btnBold)
        btnClose = findViewById(R.id.btnClose)

        // Show toolbar when content EditText is focused
        contentEditText.setOnFocusChangeListener { _, hasFocus ->
            if (isEditMode) {
                formattingToolbar.visibility = if (hasFocus) View.VISIBLE else View.GONE
            }
        }

        // Setup category & color button listeners
        categoryButton.setOnClickListener { showCategoryDialog() }
        colorButton.setOnClickListener { showColorDialog() }
    }

    private fun setupToolbarButtons() {
        // Set text as icons for heading buttons
        TextButtonHelper.setTextAsIcon(btnH1, "H₁", 36f, true)
        TextButtonHelper.setTextAsIcon(btnH2, "H₂", 34f, true)
        TextButtonHelper.setTextAsIcon(btnH3, "H₃", 32f, true)
        TextButtonHelper.setTextAsIcon(btnBold, "B", 38f, true)

        // Setup click listeners
        btnCheckList.setOnClickListener { insertCheckbox() }
        btnH1.setOnClickListener { applyHeading(1) }
        btnH2.setOnClickListener { applyHeading(2) }
        btnH3.setOnClickListener { applyHeading(3) }
        btnBold.setOnClickListener { contentEditText.applyBold() }
        btnClose.setOnClickListener {
            formattingToolbar.visibility = View.GONE
            contentEditText.clearFocus()
        }
    }

    private fun loadExistingNote() {
        existingNote = intent.getParcelableExtra("note")
        existingNote?.let { note ->
            supportActionBar?.title = note.title
            titleEditText.setText(note.title)

            val spannable = SpannableStringBuilder(note.content)
            contentEditText.setFormattedText(spannable)

            selectedCategory = note.category
            selectedColor = note.color

            updateCategoryButton()
            updateColorButton()

            // Set to view mode initially
            setViewMode()
        }

        // If creating new note, set to edit mode
        if (existingNote == null) {
            isEditMode = true
            supportActionBar?.title = getString(R.string.add_note)
            editButton.visibility = View.GONE
        }
    }

    private fun setViewMode() {
        isEditMode = false
        titleEditText.isFocusable = false
        titleEditText.isFocusableInTouchMode = false
        contentEditText.isFocusable = false
        contentEditText.isFocusableInTouchMode = false
        titleEditText.setTextIsSelectable(false)
        contentEditText.setTextIsSelectable(false)

        formattingToolbar.visibility = View.GONE
        saveButton.visibility = View.GONE
        deleteButton.visibility = View.GONE
        editButton.visibility = View.VISIBLE
    }

    private fun setEditMode() {
        isEditMode = true
        titleEditText.isFocusable = true
        titleEditText.isFocusableInTouchMode = true
        contentEditText.isFocusable = true
        contentEditText.isFocusableInTouchMode = true
        titleEditText.setTextIsSelectable(true)
        contentEditText.setTextIsSelectable(true)

        saveButton.visibility = View.VISIBLE
        deleteButton.visibility = View.VISIBLE
        editButton.visibility = View.GONE

        // Focus to content
        contentEditText.requestFocus()
    }

    private fun setupButtonListeners() {
        saveButton.setOnClickListener { saveNote() }
        editButton.setOnClickListener { setEditMode() }
        deleteButton.setOnClickListener { showDeleteConfirmation() }
    }

    private fun insertCheckbox() {
        val cursorPosition = contentEditText.selectionStart
        val currentText = contentEditText.text.toString()
        val checkbox = "☐ "

        val newText = when {
            cursorPosition == 0 -> checkbox + currentText
            currentText.getOrNull(cursorPosition - 1) == '\n' -> {
                StringBuilder(currentText).insert(cursorPosition, checkbox).toString()
            }
            else -> {
                StringBuilder(currentText).insert(cursorPosition, "\n$checkbox").toString()
            }
        }

        contentEditText.setText(newText)

        val newPosition = when {
            cursorPosition == 0 -> checkbox.length
            currentText.getOrNull(cursorPosition - 1) == '\n' -> cursorPosition + checkbox.length
            else -> cursorPosition + checkbox.length + 1
        }
        contentEditText.setSelection(newPosition)
    }

    private fun applyHeading(level: Int) {
        val start = contentEditText.selectionStart
        val end = contentEditText.selectionEnd

        if (start < 0 || end <= start) {
            Toast.makeText(this, "Pilih teks terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val spannable = contentEditText.text as? SpannableStringBuilder
            ?: SpannableStringBuilder(contentEditText.text)

        // Remove existing size spans
        val existingSizeSpans = spannable.getSpans(start, end, AbsoluteSizeSpan::class.java)
        existingSizeSpans.forEach { spannable.removeSpan(it) }

        // Remove existing style spans
        val existingStyleSpans = spannable.getSpans(start, end, StyleSpan::class.java)
        existingStyleSpans.forEach { spannable.removeSpan(it) }

        // Apply new heading style with actual pixel size
        val textSizePx = when (level) {
            1 -> (28 * resources.displayMetrics.scaledDensity).toInt() // H1: 28sp
            2 -> (24 * resources.displayMetrics.scaledDensity).toInt() // H2: 24sp
            3 -> (20 * resources.displayMetrics.scaledDensity).toInt() // H3: 20sp
            else -> (16 * resources.displayMetrics.scaledDensity).toInt() // Normal: 16sp
        }

        spannable.setSpan(
            AbsoluteSizeSpan(textSizePx),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        contentEditText.setText(spannable)
        contentEditText.setSelection(start, end)

        Toast.makeText(this, "Heading $level diterapkan", Toast.LENGTH_SHORT).show()
    }

    private fun saveNote() {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.getCurrentFormattedText().toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.empty_title_error, Toast.LENGTH_SHORT).show()
            return
        }

        if (content.isEmpty()) {
            Toast.makeText(this, R.string.empty_content_error, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (existingNote != null) {
                existingNote!!.title = title
                existingNote!!.content = content
                existingNote!!.category = selectedCategory
                existingNote!!.color = selectedColor
                repository.updateNote(existingNote!!)

                Toast.makeText(this@AddNoteActivity, R.string.note_saved, Toast.LENGTH_SHORT).show()

                // Back to view mode after save
                setViewMode()
            } else {
                val note = Note(
                    title = title,
                    content = content,
                    category = selectedCategory,
                    color = selectedColor
                )
                repository.insertNote(note)

                Toast.makeText(this@AddNoteActivity, R.string.note_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                deleteNote()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun deleteNote() {
        existingNote?.let { note ->
            lifecycleScope.launch {
                repository.deleteNote(note)
                Toast.makeText(this@AddNoteActivity, R.string.note_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showCategoryDialog() {
        if (!isEditMode) return

        val categories = arrayOf("Semua", "Pekerjaan", "Pribadi", "Belanja", "Ide", "Lainnya")

        AlertDialog.Builder(this)
            .setTitle(R.string.select_category)
            .setItems(categories) { _, which ->
                selectedCategory = categories[which]
                updateCategoryButton()
            }
            .show()
    }

    private fun updateCategoryButton() {
        categoryButton.text = "📁 $selectedCategory"
    }

    private fun showColorDialog() {
        if (!isEditMode) return

        val colors = arrayOf(
            "Putih" to "#FFFFFF",
            "Merah" to "#FFCDD2",
            "Pink" to "#F8BBD0",
            "Ungu" to "#E1BEE7",
            "Biru" to "#BBDEFB",
            "Cyan" to "#B2EBF2",
            "Hijau" to "#C8E6C9",
            "Kuning" to "#FFF9C4",
            "Orange" to "#FFE0B2"
        )

        val colorNames = colors.map { it.first }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(R.string.select_color)
            .setItems(colorNames) { _, which ->
                selectedColor = colors[which].second
                updateColorButton()
                updateBackgroundColor()
            }
            .show()
    }

    private fun updateColorButton() {
        colorButton.text = "🎨 Warna"
        try {
            colorButton.setBackgroundColor(android.graphics.Color.parseColor(selectedColor))

            // Change text color based on background brightness
            val color = android.graphics.Color.parseColor(selectedColor)
            val brightness = (android.graphics.Color.red(color) * 299 +
                    android.graphics.Color.green(color) * 587 +
                    android.graphics.Color.blue(color) * 114) / 1000

            if (brightness > 128) {
                colorButton.setTextColor(android.graphics.Color.BLACK)
            } else {
                colorButton.setTextColor(android.graphics.Color.WHITE)
            }
        } catch (e: Exception) {
            colorButton.setBackgroundColor(android.graphics.Color.WHITE)
            colorButton.setTextColor(android.graphics.Color.BLACK)
        }
    }

    private fun updateBackgroundColor() {
        try {
            window.decorView.setBackgroundColor(android.graphics.Color.parseColor(selectedColor))
        } catch (e: Exception) {
            window.decorView.setBackgroundColor(android.graphics.Color.WHITE)
        }
    }
}