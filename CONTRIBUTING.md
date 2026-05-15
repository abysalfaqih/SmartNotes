# Panduan Kontribusi — SmartNotes

Terima kasih telah tertarik berkontribusi pada SmartNotes!

---

## Sebelum Mulai

1. Pastikan kamu sudah membaca [README.md](README.md)
2. Cek [Issues](https://github.com/devnest/smartnotes/issues) yang sudah ada sebelum membuat yang baru
3. Untuk fitur besar, buat Issue terlebih dahulu untuk diskusi

---

## Setup Development

```bash
# Fork dan clone repo
git clone https://github.com/USERNAME/smartnotes.git
cd smartnotes

# Buka di Android Studio
# File > Open > pilih folder smartnotes
```

**Requirements:**
- Android Studio Hedgehog (2023.1.1)+
- JDK 21
- Android SDK 34
- Emulator atau device fisik (API 24+)

---

## Alur Kerja (Git Flow)

```
main          ← production, selalu stable
├── develop   ← integrasi fitur
│   ├── feature/nama-fitur
│   ├── fix/nama-bug
│   └── chore/nama-task
```

### Langkah Kontribusi

```bash
# 1. Buat branch dari main
git checkout -b feature/nama-fitur

# 2. Kerjakan perubahan

# 3. Commit dengan format konvensional
git commit -m "feat: tambah fitur dark mode"

# 4. Push
git push origin feature/nama-fitur

# 5. Buat Pull Request ke branch main
```

---

## Konvensi Commit

Gunakan format [Conventional Commits](https://www.conventionalcommits.org/):

| Prefix | Kapan digunakan |
|--------|----------------|
| `feat:` | Menambah fitur baru |
| `fix:` | Memperbaiki bug |
| `ui:` | Perubahan tampilan/layout |
| `refactor:` | Refactoring kode |
| `chore:` | Update dependency, konfigurasi |
| `docs:` | Update dokumentasi |
| `test:` | Menambah/update test |

**Contoh:**
```
feat: tambah dark mode support
fix: perbaiki crash saat save catatan kosong
ui: update warna primary ke teal
docs: update README dengan instruksi build
```

---

## Melaporkan Bug

Saat membuat Issue untuk bug, sertakan:

- **Versi aplikasi** (lihat di halaman Tentang)
- **Versi Android** dan **tipe device**
- **Langkah reproduksi** yang jelas
- **Behavior yang diharapkan** vs **yang terjadi**
- **Screenshot/video** jika relevan
- **Logcat** jika ada crash

---

## Mengusulkan Fitur

Saat membuat Issue untuk fitur baru:

- Jelaskan **masalah** yang ingin diselesaikan
- Deskripsikan **solusi yang diusulkan**
- Sebutkan **alternatif** yang sudah dipertimbangkan
- Tambahkan **mockup/wireframe** jika ada

---

## Checklist Pull Request

Sebelum submit PR, pastikan:

- [ ] Kode sudah ditest di emulator/device
- [ ] Tidak ada build error atau warning baru
- [ ] Mengikuti code style yang sudah ada
- [ ] Commit message mengikuti konvensi
- [ ] Update `CHANGELOG.md` jika perlu
- [ ] PR description menjelaskan perubahan dengan jelas

---

## Code Style

- Gunakan **Kotlin** (bukan Java)
- Ikuti **Kotlin coding conventions** resmi
- Variabel dan fungsi: `camelCase`
- Kelas: `PascalCase`
- Konstanta: `UPPER_SNAKE_CASE`
- Hindari komentar yang tidak perlu — tulis kode yang self-documenting

---

Terima kasih atas kontribusinya! 🙏
