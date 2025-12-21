package com.smartnotes.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.smartnotes.app.data.Note
import com.smartnotes.app.data.NoteDaoImpl
import com.smartnotes.app.data.NoteRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var repository: NoteRepository
    private lateinit var adapter: NoteAdapter
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var selectionToolbar: androidx.appcompat.widget.Toolbar
    private lateinit var fabAddNote: FloatingActionButton

    private var allNotes: List<Note> = emptyList()
    private var currentSortMode = SortMode.DATE_NEWEST

    enum class SortMode {
        DATE_NEWEST, DATE_OLDEST, TITLE_AZ, TITLE_ZA
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        selectionToolbar = findViewById(R.id.selectionToolbar)
        setSupportActionBar(toolbar)

        // Force menu to use light theme
        toolbar.popupTheme = R.style.ThemeOverlay_AppCompat_Light

        val noteDao = NoteDaoImpl(this)
        repository = NoteRepository(noteDao)

        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        searchEditText = findViewById(R.id.searchEditText)
        fabAddNote = findViewById(R.id.fabAddNote)

        adapter = NoteAdapter(
            notes = emptyList(),
            onNoteClick = { note ->
                if (!adapter.isSelectionMode) {
                    openNoteDetail(note)
                }
            },
            onNoteLongClick = { note ->
                showNoteOptionsDialog(note)
            }
        )

        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        notesRecyclerView.adapter = adapter

        fabAddNote.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchNotes(s.toString())
            }
        })

        setupSelectionToolbar()
    }

    private fun showNoteOptionsDialog(note: Note) {
        val pinOption = if (note.isPinned) "📌 Unpin" else "📌 Pin"
        val options = arrayOf(
            pinOption,
            "📁 Ubah Kategori",
            "🎨 Ubah Warna",
            "🗑️ Hapus"
        )

        AlertDialog.Builder(this)
            .setTitle(note.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> togglePinNote(note) // Pin/Unpin
                    1 -> showCategoryDialog(note) // Change Category
                    2 -> showColorDialog(note) // Change Color
                    3 -> showDeleteConfirmation(note) // Delete
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showCategoryDialog(note: Note) {
        val categories = arrayOf("Semua", "Pekerjaan", "Pribadi", "Belanja", "Ide", "Lainnya")
        val currentIndex = categories.indexOf(note.category).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this)
            .setTitle("Pilih Kategori")
            .setSingleChoiceItems(categories, currentIndex) { dialog, which ->
                lifecycleScope.launch {
                    note.category = categories[which]
                    repository.updateNote(note)
                    Toast.makeText(
                        this@MainActivity,
                        "Kategori diubah ke ${categories[which]}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadNotes()
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showColorDialog(note: Note) {
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
        val currentIndex = colors.indexOfFirst { it.second == note.color }.takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this)
            .setTitle("Pilih Warna")
            .setSingleChoiceItems(colorNames, currentIndex) { dialog, which ->
                lifecycleScope.launch {
                    note.color = colors[which].second
                    repository.updateNote(note)
                    Toast.makeText(
                        this@MainActivity,
                        "Warna diubah ke ${colors[which].first}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadNotes()
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun togglePinNote(note: Note) {
        lifecycleScope.launch {
            note.isPinned = !note.isPinned
            repository.updateNote(note)

            val message = if (note.isPinned) {
                getString(R.string.note_pinned)
            } else {
                getString(R.string.note_unpinned)
            }
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()

            loadNotes()
        }
    }

    private fun showDeleteConfirmation(note: Note) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                deleteNote(note)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun deleteNote(note: Note) {
        lifecycleScope.launch {
            repository.deleteNote(note)
            Toast.makeText(this@MainActivity, R.string.note_deleted, Toast.LENGTH_SHORT).show()
            loadNotes()
        }
    }

    private fun setupSelectionToolbar() {
        selectionToolbar.inflateMenu(R.menu.selection_menu)
        selectionToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_select_all -> {
                    adapter.selectAll()
                    updateSelectionToolbar()
                    true
                }
                R.id.action_delete_selected -> {
                    showDeleteSelectedConfirmation()
                    true
                }
                R.id.action_cancel -> {
                    exitSelectionMode()
                    true
                }
                else -> false
            }
        }

        selectionToolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        selectionToolbar.setNavigationOnClickListener {
            exitSelectionMode()
        }
    }

    private fun enterSelectionMode() {
        adapter.isSelectionMode = true
        toolbar.visibility = View.GONE
        selectionToolbar.visibility = View.VISIBLE
        fabAddNote.hide()
    }

    private fun exitSelectionMode() {
        adapter.isSelectionMode = false
        toolbar.visibility = View.VISIBLE
        selectionToolbar.visibility = View.GONE
        fabAddNote.show()
    }

    private fun updateSelectionToolbar() {
        val selectedCount = adapter.getSelectedNotes().size
        selectionToolbar.title = getString(R.string.selected_count, selectedCount)
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
        if (adapter.isSelectionMode) {
            exitSelectionMode()
        }
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            allNotes = repository.getAllNotes()
            sortAndDisplayNotes()
        }
    }

    private fun searchNotes(query: String) {
        lifecycleScope.launch {
            val notes = repository.searchNotes(query)
            allNotes = notes
            sortAndDisplayNotes()
        }
    }

    private fun sortAndDisplayNotes() {
        val sortedNotes = when (currentSortMode) {
            SortMode.DATE_NEWEST -> allNotes.sortedByDescending { it.timestamp }
            SortMode.DATE_OLDEST -> allNotes.sortedBy { it.timestamp }
            SortMode.TITLE_AZ -> allNotes.sortedBy { it.title.lowercase() }
            SortMode.TITLE_ZA -> allNotes.sortedByDescending { it.title.lowercase() }
        }
        updateUI(sortedNotes)
    }

    private fun updateUI(notes: List<Note>) {
        if (notes.isEmpty()) {
            notesRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            notesRecyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
        adapter.updateNotes(notes)
    }

    private fun openNoteDetail(note: Note) {
        val intent = Intent(this, AddNoteActivity::class.java)
        intent.putExtra("note", note)
        startActivity(intent)
    }

    private fun showDeleteSelectedConfirmation() {
        val selectedNotes = adapter.getSelectedNotes()
        if (selectedNotes.isEmpty()) {
            Toast.makeText(this, "Tidak ada catatan yang dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.delete_selected)
            .setMessage(getString(R.string.delete_selected_confirmation, selectedNotes.size))
            .setPositiveButton(R.string.yes) { _, _ ->
                deleteSelectedNotes(selectedNotes)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun deleteSelectedNotes(notes: List<Note>) {
        lifecycleScope.launch {
            notes.forEach { note ->
                repository.deleteNote(note)
            }
            Toast.makeText(
                this@MainActivity,
                getString(R.string.notes_deleted, notes.size),
                Toast.LENGTH_SHORT
            ).show()
            exitSelectionMode()
            loadNotes()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        // Make sure menu items are visible
        menu?.findItem(R.id.action_sort)?.isVisible = true
        menu?.findItem(R.id.action_about)?.isVisible = true

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_date_newest -> {
                currentSortMode = SortMode.DATE_NEWEST
                sortAndDisplayNotes()
                true
            }
            R.id.sort_date_oldest -> {
                currentSortMode = SortMode.DATE_OLDEST
                sortAndDisplayNotes()
                true
            }
            R.id.sort_title_az -> {
                currentSortMode = SortMode.TITLE_AZ
                sortAndDisplayNotes()
                true
            }
            R.id.sort_title_za -> {
                currentSortMode = SortMode.TITLE_ZA
                sortAndDisplayNotes()
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (adapter.isSelectionMode) {
            exitSelectionMode()
        } else {
            super.onBackPressed()
        }
    }
}