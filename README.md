# Dasar-Dasar FTC

Panduan belajar pemrograman **FIRST Tech Challenge** dalam Bahasa Indonesia.

Repositori ini dibuat untuk pemula yang baru masuk ke dunia FTC dan bingung harus mulai dari mana. Hampir semua tutorial FTC yang ada berbahasa Inggris, jadi tujuan repo ini sederhana: bikin materi yang bisa dipahami tanpa harus berjuang dengan bahasa dulu.

---

## Berdasarkan SDK Resmi FIRST

Proyek ini merupakan turunan dari repositori resmi FIRST Tech Challenge:

**https://github.com/FIRST-Tech-Challenge/FtcRobotController**

Seluruh kode di dalam folder `FtcRobotController/` adalah milik FIRST dan tidak dimodifikasi. Yang ditambahkan di sini hanya materi pembelajaran di dalam folder `TeamCode/`.

Versi SDK: **v11.2.1** (musim DECODE 2025–2026)

---

## Apa yang Kamu Butuhkan

| Kebutuhan | Keterangan |
|---|---|
| **JDK 17** | Eclipse Temurin — [adoptium.net](https://adoptium.net/temurin/releases/?version=17) |
| **Android Studio** | Versi Narwhal 3 Feature Drop atau lebih baru |
| **Git** | Untuk clone repositori ini |
| **Control Hub** | REV Control Hub + Driver Hub (atau HP Android) |

Kalau kamu belum punya hardware sama sekali, tenang — kamu tetap bisa menulis dan meng-compile kode. Cuma nggak bisa dites ke robot beneran.

---

## Cara Mulai

**1. Clone repositori ini**

```bash
git clone https://github.com/Audric-Tsai/Dasar-Dasar-FTC.git
cd Dasar-Dasar-FTC
```

**2. Buka di Android Studio**

`File → Open` → pilih folder yang barusan di-clone. Tunggu Gradle sync selesai. Proses pertama kali agak lama karena banyak yang harus di-download.

**3. Atur lokasi JDK kamu**

Buat file `gradle.properties` di folder user kamu (`C:\Users\NAMAKAMU\.gradle\` di Windows), lalu isi dengan path JDK 17 kamu:

```properties
org.gradle.java.home=C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.20.8-hotspot
```

Sesuaikan path-nya dengan lokasi di komputer kamu. Wajib pakai double backslash.

**4. Tes build**

```bash
./gradlew :TeamCode:assembleDebug
```

Kalau muncul `BUILD SUCCESSFUL`, berarti semua sudah beres.

---

## Struktur Folder

```
Dasar-Dasar-FTC/
├── FtcRobotController/     ← SDK resmi FIRST. Jangan diubah.
├── TeamCode/               ← Semua materi belajar ada di sini
│   └── src/main/java/org/firstinspires/ftc/teamcode/
├── gradle/
└── build.gradle
```

Kamu cuma perlu ngoprek isi folder `teamcode/`. Sisanya biarkan apa adanya.

**Penting:** nama folder `FtcRobotController` dan `TeamCode` tidak boleh diubah. Keduanya dipanggil langsung oleh `settings.gradle`, jadi kalau diganti, build-nya rusak.

---

## Lisensi

Kode SDK di dalam `FtcRobotController/` mengikuti lisensi BSD 3-Clause dari FIRST. Materi pembelajaran di dalam `TeamCode/` bebas dipakai dan dibagikan untuk keperluan belajar dan mengajar.

---

## Kontribusi

Nemu kesalahan, penjelasan yang membingungkan, atau punya ide materi baru? Silakan buka issue atau kirim pull request.
