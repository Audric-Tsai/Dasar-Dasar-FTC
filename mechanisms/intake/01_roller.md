# Roller / Compliant Wheel Intake

## Cara Kerja

Sepasang (atau lebih) roda karet lentur (compliant wheel) atau roller berputar berlawanan arah, saling berhadapan atau sejajar. Game piece masuk di antara roller, terjepit sebentar, lalu terdorong masuk ke robot karena gesekan dan putaran roller. Biasanya digerakkan langsung oleh satu motor DC lewat gear/chain, kadang lewat belt.

Contoh part yang sering dipakai: REV Flap Wheel, goBILDA Stealth Wheel, roda karet compliant dengan durometer rendah (lebih lunak = lebih banyak gesekan).

## Kelebihan

- **Cepat** — begitu game piece menyentuh roller, langsung tertarik masuk. Cycle time termasuk yang tercepat dibanding jenis intake lain.
- **Sederhana secara mekanis** — cuma butuh satu motor dan sepasang roller, nggak perlu banyak part bergerak.
- **Terus menerus (continuous)** — motor bisa terus berputar, robot tinggal "menyodok" ke arah game piece tanpa perlu koordinasi timing yang rumit.
- **Toleran terhadap posisi** — game piece nggak harus pas persis di tengah, asal masuk area roller biasanya tetap tertarik.

## Kekurangan

- **Butuh permukaan yang bisa "digigit"** — kurang cocok untuk game piece yang sangat licin, rata, atau terlalu besar untuk masuk di antara roller.
- **Bisa merusak game piece yang rapuh** — tekanan roller yang terlalu kencang bisa membuat game piece penyok atau rusak.
- **Rawan macet (jam)** — kalau dua game piece masuk bersamaan atau posisinya miring, roller bisa macet dan perlu direverse.
- **Konsumsi arus tinggi saat macet** — motor yang menahan beban macet bisa membuat lonjakan arus dan berisiko trip breaker 12V.
- **Perlu penyetelan jarak (gap) roller** — jarak antar roller harus pas dengan ketebalan game piece; kalau salah ukur, di awal musim harus banyak coba-coba.

## Kapan Sebaiknya Dipakai

Kalau game piece musim ini berbentuk bulat, silinder, atau punya permukaan yang cukup kesat (grippy), dan tim butuh cycle time cepat untuk strategi cetak skor berulang kali.
