package com.smartnotes.app

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.smartnotes.app.data.Note
import com.smartnotes.app.data.NoteDaoImpl
import com.smartnotes.app.data.NoteRepository
import kotlinx.coroutines.launch

class AddNoteActivity : AppCompatActivity() {

    private lateinit var repository: NoteRepository
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: RichEditText
    private lateinit var saveButton: ExtendedFloatingActionButton
    private lateinit var deleteButton: LinearLayout
    private lateinit var editButton: LinearLayout
    private lateinit var deleteButtonCard: MaterialCardView
    private lateinit var editButtonCard: MaterialCardView
    private lateinit var formattingToolbar: MaterialCardView
    private lateinit var categoryButton: TextView
    private lateinit var colorButton: TextView
    private lateinit var categoryCard: MaterialCardView
    private lateinit var colorCard: MaterialCardView

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
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val noteDao = NoteDaoImpl(this)
        repository = NoteRepository(noteDao)

        initializeViews()
        setupToolbarButtons()
        loadExistingNote()
        setupButtonListeners()

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Update background color
        updateBackgroundColor()
    }

    private fun initializeViews() {
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
        editButton = findViewById(R.id.editButton)
        deleteButtonCard = findViewById(R.id.deleteButtonCard)
        editButtonCard = findViewById(R.id.editButtonCard)
        formattingToolbar = findViewById(R.id.formattingToolbar)
        categoryButton = findViewById(R.id.categoryButton)
        colorButton = findViewById(R.id.colorButton)
        categoryCard = findViewById(R.id.categoryCard)
        colorCard = findViewById(R.id.colorCard)

        btnCheckList = findViewById(R.id.btnCheckList)
        btnH1 = findViewById(R.id.btnH1)
        btnH2 = findViewById(R.id.btnH2)
        btnH3 = findViewById(R.id.btnH3)
        btnBold = findViewById(R.id.btnBold)
        btnClose = findViewById(R.id.btnClose)

        // Show toolbar when content EditText is focused
        contentEditText.setOnFocusChangeListener { _, hasFocus ->
            if (isEditMode) {
                if (hasFocus) {
                    formattingToolbar.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .withStartAction { formattingToolbar.visibility = View.VISIBLE }
                        .start()
                } else {
                    formattingToolbar.animate()
                        .alpha(0f)
                        .translationY(100f)
                        .setDuration(300)
                        .withEndAction { formattingToolbar.visibility = View.GONE }
                        .start()
                }
            }
        }

        // Setup category & color button listeners
        categoryCard.setOnClickListener { showCategoryDialog() }
        colorCard.setOnClickListener { showColorDialog() }
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
            formattingToolbar.animate()
                .alpha(0f)
                .translationY(100f)
                .setDuration(300)
                .withEndAction { formattingToolbar.visibility = View.GONE }
                .start()
            contentEditText.clearFocus()
        }
    }

    private fun loadExistingNote() {
        existingNote = intent.getParcelableExtra("note")
        existingNote?.let { note ->
            titleEditText.setText(note.title)

            val spannable = SpannableStringBuilder(note.content)
            contentEditText.setFormattedText(spannable)

            selectedCategory = note.category
            selectedColor = note.color

            updateCategoryButton()
            updateColorButton()
            updateBackgroundColor()

            // Set to view mode initially
            setViewMode()
        }

        // If creating new note, set to edit mode
        if (existingNote == null) {
            isEditMode = true
            editButtonCard.visibility = View.GONE
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
        saveButton.hide()
        deleteButtonCard.visibility = View.VISIBLE
        editButtonCard.visibility = View.VISIBLE
    }

    private fun setEditMode() {
        isEditMode = true
        titleEditText.isFocusable = true
        titleEditText.isFocusableInTouchMode = true
        contentEditText.isFocusable = true
        contentEditText.isFocusableInTouchMode = true
        titleEditText.setTextIsSelectable(true)
        contentEditText.setTextIsSelectable(true)

        saveButton.show()
        deleteButtonCard.visibility = View.GONE
        editButtonCard.visibility = View.GONE

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
            1 -> (28 * resources.displayMetrics.scaledDensity).toInt()
            2 -> (24 * resources.displayMetrics.scaledDensity).toInt()
            3 -> (20 * resources.displayMetrics.scaledDensity).toInt()
            else -> (16 * resources.displayMetrics.scaledDensity).toInt()
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

        // Animate save button
        saveButton.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                saveButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()

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
            "Default" to "#FFFFFF",
            "Merah" to "#FFE5E5",
            "Pink" to "#FFE5F3",
            "Ungu" to "#F3E8FF",
            "Biru" to "#E0F2FE",
            "Cyan" to "#CFFAFE",
            "Hijau" to "#D1FAE5",
            "Kuning" to "#FEF9C3",
            "Orange" to "#FFEDD5"
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
    }

    private fun updateBackgroundColor() {
        try {
            val color = android.graphics.Color.parseColor(selectedColor)
            window.decorView.setBackgroundColor(color)

            // Update card backgrounds to match
            findViewById<MaterialCardView>(R.id.categoryCard)?.setCardBackgroundColor(color)
            findViewById<MaterialCardView>(R.id.colorCard)?.setCardBackgroundColor(color)
        } catch (e: Exception) {
            window.decorView.setBackgroundColor(android.graphics.Color.WHITE)
        }
    }
}