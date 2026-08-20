# Panduan Launcher (Shooter) FTC

Dokumen ini adalah panduan umum untuk sistem launcher/shooter di FTC — mekanisme yang melontarkan game piece ke target dari jarak jauh, alih-alih meletakkannya langsung. Setiap jenis launcher punya dua file: satu membahas cara kerja/kelebihan/kekurangan, satu lagi (`_panduan_build.md`) membahas saran wheel, tips build, dan link referensi (guide + video YouTube).

## Daftar Jenis Launcher

| File Penjelasan | File Panduan Build | Jenis Launcher | Cocok Untuk |
|---|---|---|---|
| [01_flywheel_tunggal.md](01_flywheel_tunggal.md) | [01_flywheel_tunggal_panduan_build.md](01_flywheel_tunggal_panduan_build.md) | Flywheel Tunggal | Sederhana, jarak tembak sedang, budget terbatas |
| [02_flywheel_ganda.md](02_flywheel_ganda.md) | [02_flywheel_ganda_panduan_build.md](02_flywheel_ganda_panduan_build.md) | Flywheel Ganda (Double Flywheel) | Kecepatan lontar tinggi & konsisten, kontrol spin |
| [03_indexer_flywheel.md](03_indexer_flywheel.md) | [03_indexer_flywheel_panduan_build.md](03_indexer_flywheel_panduan_build.md) | Indexer + Flywheel (Continuous Feed) | Tembak berulang cepat (cycle time tinggi) |
| [04_catapult.md](04_catapult.md) | [04_catapult_panduan_build.md](04_catapult_panduan_build.md) | Catapult / Trebuchet | Sederhana secara elektrik, sekali lontar per siklus |

## Konsep Dasar yang Perlu Dipahami

Sebelum memilih jenis launcher, ada beberapa hal fisika dasar yang menentukan performa:

1. **Energi lontar datang dari kecepatan roda/lengan saat kontak dengan game piece** — makin cepat putaran flywheel (atau makin cepat ayunan lengan catapult), makin jauh/kencang lontarannya. Tapi kecepatan yang terlalu tinggi bisa merusak game piece atau bikin lontaran nggak akurat.

2. **Konsistensi lebih penting daripada kecepatan maksimum** — robot yang selalu melontar dengan kecepatan yang SAMA persis setiap kali jauh lebih mudah diarahkan secara akurat dibanding robot yang kadang kencang kadang pelan. Flywheel yang RPM-nya "recovery" cepat (kembali ke kecepatan target setelah melontar) itu kunci.

3. **Spin-up time (waktu pemanasan)** — flywheel butuh waktu untuk mencapai RPM target sebelum siap menembak. Semakin besar/berat flywheel-nya, semakin lama waktu spin-up-nya, tapi juga semakin stabil RPM-nya saat menembak (nggak gampang "ngedrop" pas game piece masuk).

4. **Sudut lontar (launch angle)** — mempengaruhi jarak dan trajektori. Biasanya perlu disetel/ditest langsung di lapangan, bukan cuma dihitung di atas kertas.

## Cara Memilih

- **Berapa kali harus menembak per match?** Sekali-dua kali → catapult sudah cukup. Berkali-kali cepat → flywheel + indexer.
- **Seberapa jauh targetnya?** Makin jauh, makin butuh kecepatan lontar tinggi dan konsistensi RPM yang bagus (flywheel ganda lebih unggul di sini).
- **Berapa banyak game piece yang perlu disimpan sebelum ditembak?** Kalau butuh nembak beruntun dari beberapa game piece yang sudah dikumpulkan, sistem indexer/feeder jadi wajib.
- **Berapa daya listrik dan berat yang tersedia?** Flywheel butuh motor berdaya cukup besar dan biasanya lebih berat dibanding catapult yang cuma butuh servo/pegas.
