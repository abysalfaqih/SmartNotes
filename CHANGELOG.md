# Changelog

Semua perubahan penting pada proyek ini akan didokumentasikan di file ini.

Format mengikuti [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
dan proyek ini mengikuti [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2025-05-15

### Initial Release

#### Ditambahkan
- **Manajemen Catatan** — buat, baca, update, dan hapus catatan
- **Rich Text Editor** — dukungan Heading H1/H2/H3, Bold, dan Checklist
- **12 Warna Catatan** — pilihan warna pastel ala Google Keep
- **Kategori Catatan** — Semua, Pekerjaan, Pribadi, Belanja, Ide, Lainnya
- **Pin Catatan** — tandai catatan penting agar selalu berada di atas
- **Recycle Bin (Sampah)** — catatan terhapus disimpan sementara selama 30 hari
- **Pemulihan Catatan** — restore catatan dari sampah kapan saja
- **Hapus Permanen** — hapus catatan dari sampah secara manual
- **Auto-cleanup** — catatan di sampah dihapus otomatis setelah 30 hari
- **Pencarian Real-time** — cari catatan berdasarkan judul dan isi
- **Pengurutan** — urutkan catatan berdasarkan tanggal terbaru/terlama dan judul A-Z/Z-A
- **Tampilan List & Grid** — toggle antara tampilan list dan staggered grid
- **Multi-Select** — pilih banyak catatan sekaligus untuk dihapus massal
- **Bagikan Catatan** — share teks catatan ke aplikasi lain
- **Animasi Halus** — transisi dan animasi di seluruh aplikasi
- **Storage Lokal** — data tersimpan di SharedPreferences (tidak butuh internet)
- **Halaman Tentang** — informasi versi dan developer

#### Technical
- Minimum SDK: API 24 (Android 7.0 Nougat)
- Target SDK: API 34 (Android 14)
- Bahasa: Kotlin
- UI: Material Design 3
- Storage: SharedPreferences + JSON serialization
- Async: Kotlin Coroutines

---

## [Unreleased]

### Rencana Mendatang
- [ ] Dark mode
- [ ] Export catatan ke PDF/TXT
- [ ] Backup & restore data
- [ ] Widget homescreen
- [ ] Reminder/alarm untuk catatan
- [ ] Pencarian berdasarkan kategori
- [ ] Room Database migration
