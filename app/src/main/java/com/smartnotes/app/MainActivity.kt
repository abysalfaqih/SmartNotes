package com.smartnotes.app

import android.content.Intent
import android.content.SharedPreferences
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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
    private lateinit var prefs: SharedPreferences

    private var allNotes: List<Note> = emptyList()
    private var currentSortMode = SortMode.DATE_NEWEST
    private var isGridView = false

    enum class SortMode {
        DATE_NEWEST, DATE_OLDEST, TITLE_AZ, TITLE_ZA
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("smart_notes_prefs", MODE_PRIVATE)
        isGridView = prefs.getBoolean("is_grid_view", false)

        toolbar = findViewById(R.id.toolbar)
        selectionToolbar = findViewById(R.id.selectionToolbar)
        setSupportActionBar(toolbar)

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

        setupRecyclerView()

        notesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fabAddNote.isShown) {
                    fabAddNote.hide()
                } else if (dy < 0 && !fabAddNote.isShown) {
                    fabAddNote.show()
                }
            }
        })

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

        // Auto-cleanup expired trash notes on app start
        lifecycleScope.launch {
            repository.deleteExpiredNotes()
        }
    }

    private fun setupRecyclerView() {
        notesRecyclerView.layoutManager = if (isGridView) {
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            LinearLayoutManager(this)
        }
        notesRecyclerView.adapter = adapter
    }

    private fun toggleView() {
        isGridView = !isGridView
        prefs.edit().putBoolean("is_grid_view", isGridView).apply()

        notesRecyclerView.layoutManager = if (isGridView) {
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            LinearLayoutManager(this)
        }

        notesRecyclerView.adapter = adapter
        invalidateOptionsMenu()
    }

    private fun showNoteOptionsDialog(note: Note) {
        val pinOption = if (note.isPinned) "📌 Unpin" else "📌 Pin"
        val options = arrayOf(
            pinOption,
            "📁 Ubah Kategori",
            "🎨 Ubah Warna",
            "🗑️ Pindahkan ke Sampah"
        )

        AlertDialog.Builder(this)
            .setTitle(note.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> togglePinNote(note)
                    1 -> showCategoryDialog(note)
                    2 -> showColorDialog(note)
                    3 -> showMoveToTrashConfirmation(note)
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
                    loadNotes()
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showColorDialog(note: Note) {
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
        val currentIndex = colors.indexOfFirst { it.second == note.color }.takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this)
            .setTitle("Pilih Warna")
            .setSingleChoiceItems(colorNames, currentIndex) { dialog, which ->
                lifecycleScope.launch {
                    note.color = colors[which].second
                    repository.updateNote(note)
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
            loadNotes()
        }
    }

    private fun showMoveToTrashConfirmation(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Pindahkan ke Sampah")
            .setMessage("Pindahkan \"${note.title}\" ke sampah?")
            .setPositiveButton("Ya") { _, _ ->
                moveToTrash(note)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun moveToTrash(note: Note) {
        lifecycleScope.launch {
            repository.moveToTrash(note)
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
            .setTitle("Pindahkan ke Sampah")
            .setMessage("Pindahkan ${selectedNotes.size} catatan ke sampah?")
            .setPositiveButton("Ya") { _, _ ->
                deleteSelectedNotes(selectedNotes)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteSelectedNotes(notes: List<Note>) {
        lifecycleScope.launch {
            notes.forEach { note ->
                repository.moveToTrash(note)
            }
            exitSelectionMode()
            loadNotes()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // View Toggle
            R.id.view_grid -> {
                if (!isGridView) toggleView()
                true
            }
            R.id.view_list -> {
                if (isGridView) toggleView()
                true
            }
            // Sort Options
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
            R.id.action_trash -> {
                startActivity(Intent(this, TrashActivity::class.java))
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