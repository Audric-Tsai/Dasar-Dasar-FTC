# Indexer + Flywheel (Continuous Feed Launcher)

## Cara Kerja

Ini bukan jenis flywheel yang berbeda secara fisik (bisa dipasangkan ke flywheel tunggal maupun ganda), tapi tambahan sistem "indexer" atau "feeder" — biasanya servo/motor kecil dengan mekanisme kicker/gate — yang mengatur game piece masuk satu per satu ke flywheel secara terjadwal dari tempat penyimpanan (magazine/hopper), tanpa robot harus berhenti atau operator menembak manual setiap kali.

## Kelebihan

- **Cycle time paling cepat** — begitu flywheel sudah di RPM target, indexer bisa memasukkan game piece berturut-turut dengan jeda sangat singkat, cocok untuk strategi "tembak banyak dalam waktu singkat".
- **Operator cuma perlu fokus posisi & bidik** — begitu ditekan, sisanya (feeding, timing) otomatis diatur kode, mengurangi beban kerja operator saat pertandingan.
- **Bisa dikombinasikan dengan sistem penyimpanan (hopper/magazine)** — robot bisa mengumpulkan banyak game piece dulu, baru menembakkannya semua secara berurutan di waktu yang tepat.
- **RPM flywheel bisa dijaga stabil** — karena game piece masuk satu-satu dengan jeda terkontrol (bukan asal masuk sekaligus), flywheel py cukup waktu memulihkan RPM di antara tembakan.

## Kekurangan

- **Kompleksitas paling tinggi** — butuh mekanisme tambahan (servo/motor indexer, sensor untuk deteksi game piece, dan logika kode yang lebih rumit) di atas flywheel yang sudah ada.
- **Lebih banyak yang bisa rusak/macet** — makin banyak part bergerak (indexer, gate, jalur feeding), makin besar kemungkinan macet terutama kalau game piece bentuknya nggak seragam.
- **Butuh tuning software yang matang** — timing antara "flywheel siap" dan "indexer mendorong game piece" harus pas; kalau nggak, tembakan pertama dalam rentetan sering lebih lemah karena RPM belum stabil.
- **Nambah berat dan ruang** — sistem indexer/hopper menambah part di atas mekanisme flywheel yang sudah ada.

## Kapan Sebaiknya Dipakai

Kalau strategi tim adalah "farming" — kumpulkan banyak game piece dulu, lalu tembak semuanya cepat berturut-turut dalam satu waktu — dan tim sudah punya dasar flywheel (tunggal/ganda) yang bekerja dengan baik dan siap ditingkatkan ke level berikutnya.
