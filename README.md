# SmartNotes

<div align="center">

![SmartNotes Banner](https://img.shields.io/badge/SmartNotes-v1.0.0-00897B?style=for-the-badge&logo=android&logoColor=white)

**Aplikasi catatan pintar untuk Android — simpel, cepat, dan elegan.**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-blue?style=flat-square)](https://developer.android.com/about/versions/nougat)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-green?style=flat-square)](https://developer.android.com/about/versions/14)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)
[![Release](https://img.shields.io/github/v/release/devnest/smartnotes?style=flat-square&color=00897B)](https://github.com/devnest/smartnotes/releases)

[Download APK](#-download) - [Fitur](#-fitur) - [Screenshot](#-screenshot) - [Kontribusi](#-kontribusi)

</div>

---

## Download

| Versi | APK | Ukuran | Tanggal |
|-------|-----|--------|---------|
| v1.0.0 | [Download](https://github.com/devnest/smartnotes/releases/download/v1.0.0/SmartNotes-v1.0.0-release.apk) | ~3 MB | 2025 |

> **Catatan:** Aktifkan *Install from Unknown Sources* di pengaturan Android untuk menginstal APK secara manual.

---

## Fitur

### Manajemen Catatan
- ✅ Buat, edit, dan hapus catatan dengan cepat
- ✅ **Rich Text Editor** — heading (H1/H2/H3), bold, checklist
- ✅ **Warna catatan** — 12 pilihan warna pastel
- ✅ **Kategori** — Pekerjaan, Pribadi, Belanja, Ide, dll
- ✅ **Pin catatan** — tandai catatan penting di atas
- ✅ **Bagikan catatan** ke aplikasi lain

### Pencarian & Pengurutan
- ✅ Pencarian real-time berdasarkan judul & isi
- ✅ Urutkan: Terbaru, Terlama, A-Z, Z-A
- ✅ Tampilan **List** dan **Grid** (Staggered)

### Recycle Bin (Sampah)
- ✅ Catatan terhapus masuk ke **Sampah** terlebih dahulu
- ✅ Catatan di sampah otomatis terhapus permanen setelah **30 hari**
- ✅ Pulihkan catatan dari sampah kapan saja

### UI/UX
- ✅ Desain **Material Design 3** yang bersih
- ✅ Animasi halus di seluruh aplikasi
- ✅ Mode **multi-pilih** dengan bulk delete
- ✅ Tidak membutuhkan internet — **100% offline**

---

## Tech Stack

| Komponen | Teknologi |
|----------|-----------|
| Bahasa | Kotlin |
| UI | Material Design 3, View Binding |
| Layout | ConstraintLayout, RecyclerView (Staggered Grid) |
| Storage | SharedPreferences + JSON (tanpa database eksternal) |
| Async | Kotlin Coroutines + Lifecycle Scope |
| Min SDK | API 24 (Android 7.0) |

---

## Build dari Source

### Prerequisites
- Android Studio Hedgehog (2023.1.1) atau lebih baru
- JDK 21
- Android SDK 34

### Langkah

```bash
# 1. Clone repo
git clone https://github.com/devnest/smartnotes.git
cd smartnotes

# 2. Buka di Android Studio, atau build via CLI:
./gradlew assembleRelease

# APK output ada di:
# app/build/outputs/apk/release/app-release.apk
```

### Build Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

---

## Struktur Proyek

```
smartnotes/
├── app/src/main/
│   ├── java/com/smartnotes/app/
│   │   ├── data/
│   │   │   ├── Note.kt              # Model catatan
│   │   │   ├── NoteDao.kt           # Interface DAO
│   │   │   ├── NoteDaoImpl.kt       # Implementasi storage
│   │   │   └── NoteRepository.kt   # Repository layer
│   │   ├── MainActivity.kt          # Layar utama
│   │   ├── AddNoteActivity.kt       # Tambah/edit catatan
│   │   ├── TrashActivity.kt         # Recycle bin
│   │   ├── AboutActivity.kt         # Halaman tentang
│   │   ├── NoteAdapter.kt           # RecyclerView adapter
│   │   ├── TrashAdapter.kt          # Trash adapter
│   │   ├── RichEditText.kt          # Custom EditText
│   │   └── AnimationUtils.kt        # Helper animasi
│   └── res/                         # Layout, drawable, values
└── build.gradle.kts
```

---

## Kontribusi

Kontribusi sangat diterima! Silakan baca [CONTRIBUTING.md](CONTRIBUTING.md) sebelum mulai.

1. Fork repo ini
2. Buat branch baru: `git checkout -b feature/nama-fitur`
3. Commit perubahan: `git commit -m "feat: tambah fitur X"`
4. Push: `git push origin feature/nama-fitur`
5. Buat Pull Request

---

## Lisensi

Proyek ini dilisensikan di bawah [MIT License](LICENSE).

---

## 👨‍💻 Developer

Saya Sendiri

---

<div align="center">

**Devnest**

© 2025 Devnest. All rights reserved.

</div>
