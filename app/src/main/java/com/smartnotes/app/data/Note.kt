package com.smartnotes.app.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    var id: Int = 0,
    var title: String = "",
    var content: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isSelected: Boolean = false,
    var category: String = "Semua",
    var color: String = "#FFFFFF",
    var isPinned: Boolean = false,

    // NEW: Trash/Recycle Bin fields
    var isDeleted: Boolean = false,
    var deletedTimestamp: Long = 0L,

    // NEW: Rich text metadata
    var hasRichText: Boolean = false
) : Parcelable {

    companion object {
        const val TRASH_RETENTION_DAYS = 30
        const val TRASH_RETENTION_MILLIS = TRASH_RETENTION_DAYS * 24 * 60 * 60 * 1000L
    }

    fun isExpired(): Boolean {
        if (!isDeleted) return false
        val currentTime = System.currentTimeMillis()
        return (currentTime - deletedTimestamp) > TRASH_RETENTION_MILLIS
    }

    fun getDaysUntilPermanentDelete(): Int {
        if (!isDeleted) return -1
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - deletedTimestamp
        val remaining = TRASH_RETENTION_MILLIS - elapsed
        return (remaining / (24 * 60 * 60 * 1000L)).toInt()
    }
}