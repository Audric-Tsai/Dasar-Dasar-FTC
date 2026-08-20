# Dead Wheel Odometry

## Cara Kerja

"Dead wheel" artinya roda yang "mati" — nggak digerakkan motor, cuma menyentuh lantai dan berputar bebas mengikuti gerakan robot, dipasang dengan encoder buat menghitung seberapa jauh dia berputar. Biasanya dipasang 2 atau 3 buah: satu/dua menghadap arah maju-mundur (parallel/forward pod), satu menghadap arah kiri-kanan (perpendicular/strafe pod). Roda ini biasanya dipasang dengan pegas (spring-loaded) supaya selalu menempel ke lantai meski permukaan lapangan sedikit nggak rata.

## Kelebihan

- **Paling akurat** dari semua metode odometry — karena roda ini nggak dipakai buat menggerakkan robot, dia nggak pernah kena beban torsi yang bisa bikin selip, jadi perhitungan jarak jauh lebih bisa dipercaya.
- **Independen dari drivetrain** — jenis drivetrain apa pun (mecanum, tank, dll) nggak mempengaruhi akurasi, karena dead wheel bekerja terpisah total dari sistem gerak.
- **Bisa dipasang di posisi manapun** — selama menyentuh lantai dengan konsisten, dead wheel bisa diletakkan di mana saja yang paling praktis secara desain.
- **Metode paling matang & terdokumentasi** — karena paling populer, banyak library odometry (misalnya RoadRunner) dibangun khusus dengan asumsi dead wheel sebagai sumber data utama.

## Kekurangan

- **Butuh ruang fisik tambahan** — perlu tempat buat pasang roda tambahan plus mekanisme pegasnya, yang berarti mengurangi ruang buat mekanisme lain di robot.
- **Rawan rusak/bengkok kalau kena benturan** — karena posisinya sering di bagian bawah robot yang dekat lantai, gampang tersenggol saat tabrakan dengan robot lain atau elemen lapangan.
- **Perlu kalibrasi jarak antar pod (trackwidth) yang presisi** — kalau jarak antar dead wheel diukur nggak akurat, semua perhitungan posisi ikut meleset meski roda-nya sendiri bekerja sempurna.
- **Nambah part bergerak yang perlu dirawat** — pegas bisa lemah/rusak seiring waktu, roda bisa aus, butuh pengecekan berkala.

## Kapan Sebaiknya Dipakai

Kalau strategi autonomous butuh presisi tinggi dan tim punya cukup ruang desain buat memasang 2-3 roda tambahan plus mekanisme pegasnya — ini pilihan paling umum dipakai tim FTC kompetitif yang serius soal autonomous.
