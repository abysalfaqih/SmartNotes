package com.smartnotes.app.data

interface NoteDao {
    suspend fun insert(note: Note): Long
    suspend fun update(note: Note)
    suspend fun delete(note: Note)
    suspend fun getAllNotes(): List<Note>
    suspend fun getNoteById(id: Int): Note?
    suspend fun searchNotes(query: String): List<Note>
    suspend fun getNotesByCategory(category: String): List<Note>
    suspend fun getAllCategories(): List<String>

    // NEW: Trash/Recycle Bin methods
    suspend fun moveToTrash(note: Note)
    suspend fun restoreFromTrash(note: Note)
    suspend fun getAllTrashedNotes(): List<Note>
    suspend fun permanentlyDelete(note: Note)
    suspend fun emptyTrash()
    suspend fun deleteExpiredNotes()
}