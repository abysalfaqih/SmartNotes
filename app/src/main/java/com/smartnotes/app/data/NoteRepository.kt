package com.smartnotes.app.data

class NoteRepository(private val noteDao: NoteDao) {

    suspend fun insertNote(note: Note) = noteDao.insert(note)

    suspend fun updateNote(note: Note) = noteDao.update(note)

    suspend fun deleteNote(note: Note) = noteDao.delete(note)

    suspend fun getAllNotes() = noteDao.getAllNotes()

    suspend fun getNoteById(id: Int) = noteDao.getNoteById(id)

    suspend fun searchNotes(query: String) = noteDao.searchNotes(query)

    suspend fun getNotesByCategory(category: String) = noteDao.getNotesByCategory(category)

    suspend fun getAllCategories() = noteDao.getAllCategories()
}