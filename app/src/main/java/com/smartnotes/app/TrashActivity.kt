package com.smartnotes.app

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.smartnotes.app.data.Note
import com.smartnotes.app.data.NoteDaoImpl
import com.smartnotes.app.data.NoteRepository
import kotlinx.coroutines.launch

class TrashActivity : AppCompatActivity() {

    private lateinit var repository: NoteRepository
    private lateinit var adapter: TrashAdapter
    private lateinit var trashRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var fabEmptyTrash: ExtendedFloatingActionButton
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val noteDao = NoteDaoImpl(this)
        repository = NoteRepository(noteDao)

        trashRecyclerView = findViewById(R.id.trashRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        fabEmptyTrash = findViewById(R.id.fabEmptyTrash)

        adapter = TrashAdapter(
            notes = emptyList(),
            onRestoreClick = { note ->
                restoreNote(note)
            },
            onNoteLongClick = { note ->
                showPermanentDeleteDialog(note)
            }
        )

        trashRecyclerView.layoutManager = LinearLayoutManager(this)
        trashRecyclerView.adapter = adapter

        fabEmptyTrash.setOnClickListener {
            showEmptyTrashDialog()
        }

        // Auto-delete expired notes on open
        lifecycleScope.launch {
            repository.deleteExpiredNotes()
            loadTrashedNotes()
        }
    }

    override fun onResume() {
        super.onResume()
        loadTrashedNotes()
    }

    private fun loadTrashedNotes() {
        lifecycleScope.launch {
            val trashedNotes = repository.getAllTrashedNotes()
            updateUI(trashedNotes)
        }
    }

    private fun updateUI(notes: List<Note>) {
        if (notes.isEmpty()) {
            trashRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
            fabEmptyTrash.hide()
        } else {
            trashRecyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
            fabEmptyTrash.show()
        }
        adapter.updateNotes(notes)
    }

    private fun restoreNote(note: Note) {
        lifecycleScope.launch {
            repository.restoreFromTrash(note)
            loadTrashedNotes()
        }
    }

    private fun showPermanentDeleteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Permanen")
            .setMessage("Hapus \"${note.title}\" secara permanen?\n\nTindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                permanentlyDeleteNote(note)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun permanentlyDeleteNote(note: Note) {
        lifecycleScope.launch {
            repository.permanentlyDelete(note)
            loadTrashedNotes()
        }
    }

    private fun showEmptyTrashDialog() {
        lifecycleScope.launch {
            val count = repository.getAllTrashedNotes().size
            if (count == 0) {
                Toast.makeText(this@TrashActivity, "Sampah sudah kosong", Toast.LENGTH_SHORT).show()
                return@launch
            }

            AlertDialog.Builder(this@TrashActivity)
                .setTitle("Kosongkan Sampah")
                .setMessage("Hapus semua $count catatan di sampah secara permanen?\n\nTindakan ini tidak dapat dibatalkan.")
                .setPositiveButton("Kosongkan") { _, _ ->
                    emptyTrash()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun emptyTrash() {
        lifecycleScope.launch {
            repository.emptyTrash()
            loadTrashedNotes()
        }
    }
}