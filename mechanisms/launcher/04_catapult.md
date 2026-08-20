# Catapult / Trebuchet

## Cara Kerja

Sebuah lengan diputar/diayunkan dengan cepat oleh servo, motor, atau energi yang disimpan di pegas/karet elastis (surgical tubing, rubber band), melontarkan satu game piece yang diletakkan di ujung lengan atau di dalam cekungan (cup) di ujung lengan. Berbeda dari flywheel yang mengandalkan gesekan roda berputar, catapult mengandalkan momentum ayunan lengan secara langsung.

## Kelebihan

- **Sederhana secara elektrik** — nggak butuh motor berkecepatan tinggi yang terus menyala; servo atau motor biasa dengan torsi cukup sudah bisa menggerakkan lengan.
- **Hemat daya saat idle** — energi cuma dipakai saat melontar (dan saat menarik ulang lengan/pegas), beda dengan flywheel yang biasanya terus berputar menunggu giliran tembak.
- **Konsisten kalau desainnya presisi** — karena mekanismenya "mekanis murni" (bukan gesekan roda-ke-bola yang bisa selip), asal titik lepas game piece-nya presisi, lontarannya bisa sangat konsisten tembakan demi tembakan.
- **Lebih toleran terhadap variasi permukaan game piece** — nggak bergantung pada gesekan seperti flywheel, jadi game piece licin atau nggak seragam pun bisa dilontar asal muat di cup/lengan.

## Kekurangan

- **Lambat per siklus** — perlu waktu untuk menarik ulang lengan/pegas ke posisi awal (reset) sebelum bisa menembak lagi, jauh lebih lambat dibanding flywheel + indexer.
- **Kapasitas satu per satu** — hampir selalu cuma bisa melontar satu game piece per siklus, nggak cocok untuk strategi tembak beruntun cepat.
- **Butuh ruang ayun yang cukup besar** — lengan catapult perlu ruang gerak yang lumayan luas di dalam batas ukuran robot, kadang jadi tantangan desain.
- **Trayektori kurang bisa disetel di tengah pertandingan** — sudut dan jarak lontar biasanya ditentukan dari desain fisik (panjang lengan, kekuatan pegas), jadi kurang fleksibel dibanding flywheel yang RPM-nya bisa diubah dari kode kapan saja.

## Kapan Sebaiknya Dipakai

Kalau strategi tim cuma butuh menembak satu-dua kali per match (bukan tembak beruntun cepat), atau ingin sistem yang hemat daya dan sederhana secara elektrik, dan bersedia menghabiskan waktu tuning mekanis untuk presisi lontaran.
