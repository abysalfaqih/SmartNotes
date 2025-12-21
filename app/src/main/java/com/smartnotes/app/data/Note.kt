package com.smartnotes.app.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    var id: Int = 0,
    var title: String = "",
    var content: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isSelected: Boolean = false,  // Untuk fitur multi-select
    var category: String = "Semua",  // Kategori catatan
    var color: String = "#FFFFFF",  // Warna catatan (hex)
    var isPinned: Boolean = false  // Pin status
) : Parcelable