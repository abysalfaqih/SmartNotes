package com.smartnotes.app

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
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
    private var titleKeyListener: android.text.method.KeyListener? = null
    private var contentKeyListener: android.text.method.KeyListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_add_note)

            onBackPressedDispatcher.addCallback(
                this,
                object : androidx.activity.OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        handleBackPress()
                    }
                }
            )

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

            titleKeyListener = titleEditText.keyListener
            contentKeyListener = contentEditText.keyListener

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
                // New note - langsung edit mode
                isEditMode = true
                editButton.visibility = View.GONE
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

            // Disable editing completely
            titleEditText.isFocusable = false
            titleEditText.isFocusableInTouchMode = false
            titleEditText.isEnabled = false
            titleEditText.isCursorVisible = false
            titleEditText.keyListener = null

            contentEditText.isFocusable = false
            contentEditText.isFocusableInTouchMode = false
            contentEditText.isEnabled = false
            contentEditText.isCursorVisible = false
            contentEditText.keyListener = null

            // Make text selectable for copy-paste
            titleEditText.setTextIsSelectable(true)
            contentEditText.setTextIsSelectable(true)

            // Update buttons - Edit button muncul di pojok kanan atas
            formattingToolbar.visibility = View.GONE
            saveButton.visibility = View.GONE
            editButton.visibility = View.VISIBLE

            // Invalidate menu untuk menampilkan options (termasuk Hapus)
            invalidateOptionsMenu()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setEditMode() {
        try {
            isEditMode = true

            // Enable editing
            titleEditText.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                isEnabled = true
                isCursorVisible = true
                keyListener = titleKeyListener
            }

            contentEditText.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                isEnabled = true
                isCursorVisible = true
                keyListener = contentKeyListener
            }

            // Update buttons - Save button muncul di pojok kanan atas
            saveButton.visibility = View.VISIBLE
            editButton.visibility = View.GONE

            // Hide menu options saat edit mode
            invalidateOptionsMenu()

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Options Menu - untuk Hapus dan opsi lainnya
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Hanya tampilkan menu saat View Mode
        if (!isEditMode && existingNote != null) {
            menuInflater.inflate(R.menu.note_detail_menu, menu)

            // Update pin/unpin text
            existingNote?.let { note ->
                menu?.findItem(R.id.action_pin)?.title = if (note.isPinned) "Unpin" else "Pin"
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteConfirmation()
                true
            }
            R.id.action_share -> {
                shareNote()
                true
            }
            R.id.action_pin -> {
                togglePin()
                true
            }
            R.id.action_category -> {
                showCategoryDialog()
                true
            }
            R.id.action_color -> {
                showColorDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
                return
            }

            val currentText = contentEditText.text
            if (currentText == null) {
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

                        originalTitle = title
                        originalContent = content
                        hasUnsavedChanges = false

                        Toast.makeText(this@AddNoteActivity, "Catatan disimpan", Toast.LENGTH_SHORT).show()
                        setViewMode()
                    } else {
                        val note = Note(
                            title = title,
                            content = content,
                            category = selectedCategory,
                            color = selectedColor
                        )
                        repository.insertNote(note)
                        Toast.makeText(this@AddNoteActivity, "Catatan dibuat", Toast.LENGTH_SHORT).show()
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
                .setTitle("Pindahkan ke Sampah")
                .setMessage("Pindahkan catatan ini ke sampah?")
                .setPositiveButton("Ya") { _, _ ->
                    moveToTrash()
                }
                .setNegativeButton("Batal", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun moveToTrash() {
        try {
            existingNote?.let { note ->
                lifecycleScope.launch {
                    try {
                        repository.moveToTrash(note)
                        Toast.makeText(this@AddNoteActivity, "Dipindahkan ke sampah", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@AddNoteActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareNote() {
        try {
            existingNote?.let { note ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, note.title)
                    putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.content}")
                }
                startActivity(Intent.createChooser(shareIntent, "Bagikan Catatan"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error berbagi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePin() {
        try {
            existingNote?.let { note ->
                lifecycleScope.launch {
                    note.isPinned = !note.isPinned
                    repository.updateNote(note)
                    val message = if (note.isPinned) "Catatan di-pin" else "Catatan di-unpin"
                    Toast.makeText(this@AddNoteActivity, message, Toast.LENGTH_SHORT).show()
                    invalidateOptionsMenu()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCategoryDialog() {
        try {
            val categories = arrayOf("Semua", "Pekerjaan", "Pribadi", "Belanja", "Ide", "Lainnya")
            val currentIndex = categories.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0

            AlertDialog.Builder(this)
                .setTitle("Pilih Kategori")
                .setSingleChoiceItems(categories, currentIndex) { dialog, which ->
                    selectedCategory = categories[which]
                    existingNote?.let { note ->
                        lifecycleScope.launch {
                            note.category = selectedCategory
                            repository.updateNote(note)
                            Toast.makeText(this@AddNoteActivity, "Kategori diubah", Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Batal", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showColorDialog() {
        try {
            val colors = arrayOf(
                "Default" to "#FFFFFF",
                "Merah" to "#F28B82",
                "Orange" to "#FBBC04",
                "Kuning" to "#FFF475",
                "Hijau" to "#CCFF90",
                "Teal" to "#A7FFEB",
                "Biru" to "#CBF0F8",
                "Biru Tua" to "#AECBFA",
                "Ungu" to "#D7AEFB",
                "Pink" to "#FDCFE8",
                "Coklat" to "#E6C9A8",
                "Abu-abu" to "#E8EAED"
            )

            val colorNames = colors.map { it.first }.toTypedArray()
            val currentIndex = colors.indexOfFirst { it.second == selectedColor }.takeIf { it >= 0 } ?: 0

            AlertDialog.Builder(this)
                .setTitle("Pilih Warna")
                .setSingleChoiceItems(colorNames, currentIndex) { dialog, which ->
                    selectedColor = colors[which].second
                    updateBackgroundColor()
                    existingNote?.let { note ->
                        lifecycleScope.launch {
                            note.color = selectedColor
                            repository.updateNote(note)
                            Toast.makeText(this@AddNoteActivity, "Warna diubah", Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Batal", null)
                .show()
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
}