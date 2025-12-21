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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.smartnotes.app.data.Note
import com.smartnotes.app.data.NoteDaoImpl
import com.smartnotes.app.data.NoteRepository
import kotlinx.coroutines.launch

class AddNoteActivity : AppCompatActivity() {

    private lateinit var repository: NoteRepository
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: RichEditText
    private lateinit var saveButton: FloatingActionButton
    private lateinit var deleteButton: MaterialButton
    private lateinit var editButton: MaterialButton
    private lateinit var formattingToolbar: MaterialCardView

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

    // Track if content has changed
    private var hasUnsavedChanges = false
    private var originalTitle = ""
    private var originalContent = ""

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
        setupTextChangeListeners()

        toolbar.setNavigationOnClickListener {
            handleBackPress()
        }

        updateBackgroundColor()
    }

    private fun initializeViews() {
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
        editButton = findViewById(R.id.editButton)
        formattingToolbar = findViewById(R.id.formattingToolbar)

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
                        .setDuration(200)
                        .withStartAction { formattingToolbar.visibility = View.VISIBLE }
                        .start()
                } else {
                    formattingToolbar.animate()
                        .alpha(0f)
                        .translationY(50f)
                        .setDuration(200)
                        .withEndAction { formattingToolbar.visibility = View.GONE }
                        .start()
                }
            }
        }
    }

    private fun setupToolbarButtons() {
        TextButtonHelper.setTextAsIcon(btnH1, "H₁", 36f, true)
        TextButtonHelper.setTextAsIcon(btnH2, "H₂", 34f, true)
        TextButtonHelper.setTextAsIcon(btnH3, "H₃", 32f, true)
        TextButtonHelper.setTextAsIcon(btnBold, "B", 38f, true)

        btnCheckList.setOnClickListener { insertCheckbox() }
        btnH1.setOnClickListener { applyHeading(1) }
        btnH2.setOnClickListener { applyHeading(2) }
        btnH3.setOnClickListener { applyHeading(3) }
        btnBold.setOnClickListener {
            val start = contentEditText.selectionStart
            val end = contentEditText.selectionEnd

            if (start >= 0 && end > start) {
                contentEditText.applyBold()
                hasUnsavedChanges = true
            } else {
                Toast.makeText(this, "Pilih teks terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
        btnClose.setOnClickListener {
            formattingToolbar.animate()
                .alpha(0f)
                .translationY(50f)
                .setDuration(200)
                .withEndAction { formattingToolbar.visibility = View.GONE }
                .start()
            contentEditText.clearFocus()
        }
    }

    private fun setupTextChangeListeners() {
        titleEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                if (isEditMode) {
                    hasUnsavedChanges = titleEditText.text.toString() != originalTitle
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        contentEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                if (isEditMode) {
                    hasUnsavedChanges = contentEditText.text.toString() != originalContent
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadExistingNote() {
        existingNote = intent.getParcelableExtra("note")
        existingNote?.let { note ->
            titleEditText.setText(note.title)
            contentEditText.setText(note.content)

            originalTitle = note.title
            originalContent = note.content

            selectedCategory = note.category
            selectedColor = note.color

            updateBackgroundColor()
            setViewMode()
        }

        if (existingNote == null) {
            isEditMode = true
            editButton.visibility = View.GONE
            hasUnsavedChanges = false
        }
    }

    private fun setViewMode() {
        isEditMode = false
        hasUnsavedChanges = false

        titleEditText.isFocusable = false
        titleEditText.isFocusableInTouchMode = false
        contentEditText.isFocusable = false
        contentEditText.isFocusableInTouchMode = false
        titleEditText.setTextIsSelectable(true)
        contentEditText.setTextIsSelectable(true)

        formattingToolbar.visibility = View.GONE
        saveButton.hide()
        deleteButton.visibility = View.VISIBLE
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

        saveButton.show()
        deleteButton.visibility = View.GONE
        editButton.visibility = View.GONE

        contentEditText.requestFocus()
    }

    private fun setupButtonListeners() {
        saveButton.setOnClickListener { saveNote() }
        editButton.setOnClickListener { setEditMode() }
        deleteButton.setOnClickListener { showDeleteConfirmation() }
    }

    private fun insertCheckbox() {
        val cursorPosition = contentEditText.selectionStart
        if (cursorPosition < 0) return

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
        hasUnsavedChanges = true
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

        // Remove existing spans
        val existingSizeSpans = spannable.getSpans(start, end, AbsoluteSizeSpan::class.java)
        existingSizeSpans.forEach { spannable.removeSpan(it) }

        val existingStyleSpans = spannable.getSpans(start, end, StyleSpan::class.java)
        existingStyleSpans.forEach { spannable.removeSpan(it) }

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
        hasUnsavedChanges = true

        Toast.makeText(this, "Heading $level diterapkan", Toast.LENGTH_SHORT).show()
    }

    private fun saveNote() {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.empty_title_error, Toast.LENGTH_SHORT).show()
            return
        }

        if (content.isEmpty()) {
            Toast.makeText(this, R.string.empty_content_error, Toast.LENGTH_SHORT).show()
            return
        }

        saveButton.animate()
            .scaleX(0.85f)
            .scaleY(0.85f)
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

                originalTitle = title
                originalContent = content
                hasUnsavedChanges = false

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

    private fun updateBackgroundColor() {
        try {
            val color = android.graphics.Color.parseColor(selectedColor)
            window.decorView.setBackgroundColor(color)
        } catch (e: Exception) {
            window.decorView.setBackgroundColor(android.graphics.Color.WHITE)
        }
    }

    private fun handleBackPress() {
        if (hasUnsavedChanges) {
            AlertDialog.Builder(this)
                .setTitle("Catatan Belum Disimpan")
                .setMessage("Anda memiliki perubahan yang belum disimpan. Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Simpan") { _, _ ->
                    saveNote()
                }
                .setNegativeButton("Buang") { _, _ ->
                    finish()
                }
                .setNeutralButton("Batal", null)
                .show()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        handleBackPress()
    }
}