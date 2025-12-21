package com.smartnotes.app

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.smartnotes.app.data.Note
import com.smartnotes.app.data.NoteDaoImpl
import com.smartnotes.app.data.NoteRepository
import kotlinx.coroutines.launch

class AddNoteActivity : AppCompatActivity() {

    private lateinit var repository: NoteRepository
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: RichEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var deleteButton: MaterialButton
    private lateinit var editButton: MaterialButton
    private lateinit var formattingToolbar: LinearLayout

    private lateinit var btnCheckList: LinearLayout
    private lateinit var btnH1: LinearLayout
    private lateinit var btnH2: LinearLayout
    private lateinit var btnH3: LinearLayout
    private lateinit var btnBold: LinearLayout
    private lateinit var btnClose: LinearLayout

    private var existingNote: Note? = null
    private var isEditMode = false
    private var selectedCategory = "Semua"
    private var selectedColor = "#FFFFFF"

    private var hasUnsavedChanges = false
    private var originalTitle = ""
    private var originalContent = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initializeViews() {
        try {
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

            // Show toolbar when content is focused
            contentEditText.setOnFocusChangeListener { _, hasFocus ->
                if (isEditMode && hasFocus) {
                    formattingToolbar.visibility = View.VISIBLE
                }
            }

            titleEditText.setOnFocusChangeListener { _, hasFocus ->
                if (isEditMode && !hasFocus && contentEditText.hasFocus()) {
                    formattingToolbar.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error initializing views: ${e.message}")
        }
    }

    private fun setupToolbarButtons() {
        try {
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
                    Toast.makeText(this, "Bold diterapkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Pilih teks terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            }

            btnClose.setOnClickListener {
                formattingToolbar.visibility = View.GONE
                contentEditText.clearFocus()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error setting up toolbar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTextChangeListeners() {
        try {
            titleEditText.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    if (isEditMode) {
                        val currentTitle = titleEditText.text?.toString() ?: ""
                        val currentContent = contentEditText.text?.toString() ?: ""
                        hasUnsavedChanges = currentTitle != originalTitle || currentContent != originalContent
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            contentEditText.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    if (isEditMode) {
                        val currentTitle = titleEditText.text?.toString() ?: ""
                        val currentContent = contentEditText.text?.toString() ?: ""
                        hasUnsavedChanges = currentTitle != originalTitle || currentContent != originalContent
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadExistingNote() {
        try {
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
                // New note
                isEditMode = true
                editButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
                saveButton.visibility = View.VISIBLE
                hasUnsavedChanges = false

                // Request focus to show keyboard
                titleEditText.requestFocus()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading note: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setViewMode() {
        try {
            isEditMode = false
            hasUnsavedChanges = false

            titleEditText.isFocusable = false
            titleEditText.isFocusableInTouchMode = false
            contentEditText.isFocusable = false
            contentEditText.isFocusableInTouchMode = false
            titleEditText.setTextIsSelectable(true)
            contentEditText.setTextIsSelectable(true)

            formattingToolbar.visibility = View.GONE
            saveButton.visibility = View.GONE
            deleteButton.visibility = View.VISIBLE
            editButton.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setEditMode() {
        try {
            isEditMode = true

            titleEditText.isFocusable = true
            titleEditText.isFocusableInTouchMode = true
            contentEditText.isFocusable = true
            contentEditText.isFocusableInTouchMode = true
            titleEditText.setTextIsSelectable(true)
            contentEditText.setTextIsSelectable(true)

            saveButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE
            editButton.visibility = View.GONE

            // Show keyboard
            contentEditText.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupButtonListeners() {
        try {
            saveButton.setOnClickListener { saveNote() }
            editButton.setOnClickListener { setEditMode() }
            deleteButton.setOnClickListener { showDeleteConfirmation() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun insertCheckbox() {
        try {
            val cursorPosition = contentEditText.selectionStart
            if (cursorPosition < 0) {
                contentEditText.requestFocus()
                contentEditText.text?.let {
                    contentEditText.setSelection(it.length)
                }
                return
            }

            val currentText = contentEditText.text?.toString() ?: ""
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

            Toast.makeText(this, "Checkbox ditambahkan", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyHeading(level: Int) {
        try {
            val start = contentEditText.selectionStart
            val end = contentEditText.selectionEnd

            if (start < 0 || end <= start) {
                Toast.makeText(this, "Pilih teks terlebih dahulu", Toast.LENGTH_SHORT).show()
                return
            }

            val currentText = contentEditText.text
            if (currentText == null) {
                Toast.makeText(this, "Tidak ada teks", Toast.LENGTH_SHORT).show()
                return
            }

            val spannable = if (currentText is SpannableStringBuilder) {
                currentText
            } else {
                SpannableStringBuilder(currentText)
            }

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
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error applying heading: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNote() {
        try {
            val title = titleEditText.text?.toString()?.trim() ?: ""
            val content = contentEditText.text?.toString()?.trim() ?: ""

            if (title.isEmpty()) {
                Toast.makeText(this, R.string.empty_title_error, Toast.LENGTH_SHORT).show()
                titleEditText.requestFocus()
                return
            }

            if (content.isEmpty()) {
                Toast.makeText(this, R.string.empty_content_error, Toast.LENGTH_SHORT).show()
                contentEditText.requestFocus()
                return
            }

            // Button animation
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
                try {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@AddNoteActivity, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation() {
        try {
            AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes) { _, _ ->
                    deleteNote()
                }
                .setNegativeButton(R.string.no, null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteNote() {
        try {
            existingNote?.let { note ->
                lifecycleScope.launch {
                    try {
                        repository.deleteNote(note)
                        Toast.makeText(this@AddNoteActivity, R.string.note_deleted, Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@AddNoteActivity, "Error deleting: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleBackPress()
    }
}