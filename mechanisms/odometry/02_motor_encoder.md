# Motor Encoder Odometry

## Cara Kerja

Memanfaatkan encoder yang sudah menempel di motor drivetrain (motor yang sama yang dipakai buat menggerakkan robot) buat menghitung posisi — tanpa roda atau sensor tambahan sama sekali. Sama seperti metode encoder di `AutoMajuSimple` (metode 2-5) yang menghitung jarak dari jumlah putaran motor, cuma di sini dipakai buat melacak posisi X/Y penuh, bukan cuma jarak lurus.

## Kelebihan

- **Nggak butuh part tambahan** — encoder motor drivetrain memang sudah ada bawaan (built-in) di hampir semua motor FTC (REV Core Hex, HD Hex, goBILDA Yellow Jacket), jadi metode ini "gratis" secara hardware.
- **Nggak makan ruang** — karena nggak ada roda atau sensor fisik tambahan, nggak mengurangi ruang desain buat mekanisme lain.
- **Paling sederhana buat mulai belajar** — konsepnya paling gampang dipahami pemula karena langsung memakai apa yang sudah dipelajari dari encoder motor biasa.

## Kekurangan

- **Paling nggak akurat** — karena roda drivetrain yang sama dipakai buat gerak DAN diukur, setiap kali roda selip (misalnya pas akselerasi cepat, nabrak sesuatu, atau permukaan lapangan licin), perhitungan posisi langsung meleset.
- **Akumulasi error (drift) paling cepat** — karena sumber datanya paling nggak stabil, kesalahan kecil menumpuk lebih cepat dibanding dead wheel atau sensor optik, terutama di match yang panjang.
- **Sangat bergantung jenis drivetrain** — perhitungan buat mecanum vs tank vs drivetrain lain beda rumusnya dan masing-masing py karakteristik selip yang berbeda, bikin implementasinya lebih rumit untuk hasil yang tetap kurang akurat.
- **Nggak cocok buat strategi yang butuh presisi tinggi** — misalnya parkir presisi di ruang sempit atau autonomous dengan banyak titik berhenti berurutan.

## Kapan Sebaiknya Dipakai

Cuma buat awal belajar konsep odometry, atau kalau strategi autonomous robot cuma butuh pergerakan kasar (nggak butuh presisi tinggi) dan tim belum siap investasi ruang/biaya buat dead wheel atau sensor optik. Untuk robot kompetisi serius, metode ini biasanya di-upgrade ke salah satu metode lain begitu ada kesempatan.
