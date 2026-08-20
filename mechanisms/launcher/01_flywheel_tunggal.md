# Flywheel Tunggal (Single Flywheel Launcher)

## Cara Kerja

Satu roda (flywheel) berputar cepat digerakkan motor. Game piece didorong masuk menyentuh sisi flywheel yang berputar, gesekan antara roda dan game piece mempercepat game piece sampai terlontar keluar. Biasanya ada backstop/dinding di sisi berlawanan supaya game piece "terjepit" sesaat dan menyerap energi putaran flywheel secara maksimal.

## Kelebihan

- **Paling sederhana** — cuma butuh satu motor dan satu roda, paling sedikit part bergerak dibanding jenis launcher lain.
- **Ringan dan hemat tempat** — nggak perlu ruang untuk roda kedua atau mekanisme indexer.
- **Murah** — cuma satu motor yang perlu dibeli/dipakai, cocok kalau budget atau jumlah motor yang tersedia terbatas.
- **Gampang di-debug** — karena mekanismenya sederhana, lebih gampang mencari tahu kenapa performanya nggak konsisten.

## Kekurangan

- **Kontak dengan game piece cuma satu sisi** — energi yang ditransfer ke game piece kurang maksimal dibanding flywheel ganda yang menjepit dari dua sisi, jadi butuh RPM lebih tinggi untuk hasil lontaran yang sama jauhnya.
- **Bisa menyebabkan spin/putaran liar pada game piece** — karena cuma didorong dari satu sisi, game piece bisa berputar nggak beraturan saat melayang, mempengaruhi akurasi.
- **RPM gampang drop saat menembak** — flywheel tunggal biasanya lebih ringan (momen inersia lebih kecil), jadi begitu game piece masuk, RPM-nya turun drastis sebelum sempat pulih untuk tembakan berikutnya.
- **Kurang presisi di jarak jauh** — karena kombinasi dua masalah di atas (spin liar + RPM nggak stabil), akurasi menurun kalau target jauh.

## Kapan Sebaiknya Dipakai

Kalau target relatif dekat, jumlah tembakan per match sedikit, dan tim ingin sistem paling sederhana dan murah untuk mulai belajar mekanisme flywheel sebelum upgrade ke sistem yang lebih kompleks.
