package com.smartnotes.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class NoteDaoImpl(context: Context) : NoteDao {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_notes_prefs", Context.MODE_PRIVATE)

    private val NOTES_KEY = "notes"
    private val ID_COUNTER_KEY = "id_counter"

    override suspend fun insert(note: Note): Long = withContext(Dispatchers.IO) {
        val notes = getAllNotesSync().toMutableList()
        val newId = getNextId()
        note.id = newId
        note.timestamp = System.currentTimeMillis()
        note.isDeleted = false
        notes.add(note)
        saveNotes(notes)
        return@withContext newId.toLong()
    }

    override suspend fun update(note: Note) = withContext(Dispatchers.IO) {
        val notes = getAllNotesSync().toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            note.timestamp = System.currentTimeMillis()
            notes[index] = note
            saveNotes(notes)
        }
    }

    override suspend fun delete(note: Note) = withContext(Dispatchers.IO) {
        val notes = getAllNotesSync().toMutableList()
        notes.removeAll { it.id == note.id }
        saveNotes(notes)
    }

    override suspend fun getAllNotes(): List<Note> = withContext(Dispatchers.IO) {
        return@withContext getAllNotesSync().filter { !it.isDeleted }
    }

    override suspend fun getNoteById(id: Int): Note? = withContext(Dispatchers.IO) {
        return@withContext getAllNotesSync().find { it.id == id && !it.isDeleted }
    }

    override suspend fun searchNotes(query: String): List<Note> = withContext(Dispatchers.IO) {
        val allNotes = getAllNotesSync().filter { !it.isDeleted }
        return@withContext if (query.isBlank()) {
            allNotes
        } else {
            allNotes.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun getNotesByCategory(category: String): List<Note> = withContext(Dispatchers.IO) {
        val allNotes = getAllNotesSync().filter { !it.isDeleted }
        return@withContext if (category == "Semua") {
            allNotes
        } else {
            allNotes.filter { it.category == category }
        }
    }

    override suspend fun getAllCategories(): List<String> = withContext(Dispatchers.IO) {
        val allNotes = getAllNotesSync().filter { !it.isDeleted }
        val categories = allNotes.map { it.category }.distinct().sorted().toMutableList()
        if (!categories.contains("Semua")) {
            categories.add(0, "Semua")
        }
        return@withContext categories
    }

    // NEW: Trash/Recycle Bin implementations
    override suspend fun moveToTrash(note: Note) = withContext(Dispatchers.IO) {
        val notes = getAllNotesSync().toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes[index].isDeleted = true
            notes[index].deletedTimestamp = System.currentTimeMillis()
            notes[index].isPinned = false // Unpin when moved to trash
            saveNotes(notes)
        }
    }

    override suspend fun restoreFromTrash(note: Note) = withContext(Dispatchers.IO) {
        val notes = getAllNotesSync().toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes[index].isDeleted = false
            notes[index].deletedTimestamp = 0L
            notes[index].timestamp = System.currentTimeMillis()
            saveNotes(notes)
        }
    }

    override suspend fun getAllTrashedNotes(): List<Note> = withContext(Dispatchers.IO) {
        return@withContext getAllNotesSync()
            .filter { it.isDeleted }
            .sortedByDescending { it.deletedTimestamp }
    }

    override suspend fun permanentlyDelete(note: Note) = withContext(Dispatchers.IO) {
        delete(note)
    }

    override suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        val notes = getAllNotesSync().toMutableList()
        notes.removeAll { it.isDeleted }
        saveNotes(notes)
    }

    override suspend fun deleteExpiredNotes() = withContext(Dispatchers.IO) {
        val notes = getAllNotesSync().toMutableList()
        notes.removeAll { it.isExpired() }
        saveNotes(notes)
    }

    private fun getAllNotesSync(): List<Note> {
        val notesJson = prefs.getString(NOTES_KEY, "[]") ?: "[]"
        val jsonArray = JSONArray(notesJson)
        val notes = mutableListOf<Note>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            notes.add(
                Note(
                    id = jsonObject.getInt("id"),
                    title = jsonObject.getString("title"),
                    content = jsonObject.getString("content"),
                    timestamp = jsonObject.getLong("timestamp"),
                    isSelected = false,
                    category = jsonObject.optString("category", "Semua"),
                    color = jsonObject.optString("color", "#FFFFFF"),
                    isPinned = jsonObject.optBoolean("isPinned", false),
                    isDeleted = jsonObject.optBoolean("isDeleted", false),
                    deletedTimestamp = jsonObject.optLong("deletedTimestamp", 0L),
                    hasRichText = jsonObject.optBoolean("hasRichText", false)
                )
            )
        }

        return notes.sortedWith(
            compareByDescending<Note> { !it.isDeleted }
                .thenByDescending { it.isPinned }
                .thenByDescending { it.timestamp }
        )
    }

    private fun saveNotes(notes: List<Note>) {
        val jsonArray = JSONArray()
        notes.forEach { note ->
            val jsonObject = JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("content", note.content)
                put("timestamp", note.timestamp)
                put("category", note.category)
                put("color", note.color)
                put("isPinned", note.isPinned)
                put("isDeleted", note.isDeleted)
                put("deletedTimestamp", note.deletedTimestamp)
                put("hasRichText", note.hasRichText)
            }
            jsonArray.put(jsonObject)
        }

        prefs.edit().putString(NOTES_KEY, jsonArray.toString()).apply()
    }

    private fun getNextId(): Int {
        val currentId = prefs.getInt(ID_COUNTER_KEY, 0)
        val nextId = currentId + 1
        prefs.edit().putInt(ID_COUNTER_KEY, nextId).apply()
        return nextId
    }
}