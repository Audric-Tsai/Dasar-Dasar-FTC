# Panduan Odometry FTC

## Apa Itu Odometry?

Odometry adalah cara robot tahu **posisinya sendiri** di lapangan (koordinat X, Y, dan arah hadap/heading) secara terus-menerus selama match, tanpa perlu kamera eksternal atau GPS. Robot menghitung posisinya berdasarkan seberapa jauh dan ke arah mana dia sudah bergerak sejak titik awal (start position) — mirip cara orang menghitung langkah kaki buat memperkirakan sudah berjalan berapa jauh, cuma jauh lebih presisi dan dilakukan ribuan kali per detik oleh sensor.

## Kenapa Ini Penting?

- **Autonomous presisi** — tanpa tahu posisi sendiri secara akurat, robot cuma bisa gerak berdasarkan waktu/asumsi (lihat `mechanisms/` folder lain untuk contoh `AutoMajuSimple` metode 1 di awal belajar) yang gampang meleset kalau ada gesekan/baterai lemah/dorongan robot lain.
- **Navigasi ke titik tertentu (pathing)** — dengan tahu posisi real-time, robot bisa diprogram untuk "pergi ke koordinat (X, Y)" secara langsung, bukan cuma "maju sekian detik lalu belok".
- **Koreksi otomatis** — kalau robot terdorong/melenceng dari jalur (misalnya nabrak robot lain), sistem odometry yang bagus bisa mendeteksi itu dan robot bisa mengoreksi rutenya sendiri.
- **TeleOp berbantuan (driver-assist)** — beberapa tim pakai odometry buat fitur seperti "auto-align ke target" bahkan saat driver yang mengendalikan robot secara manual.

## Jenis-Jenis Metode Odometry

| File | Metode | Karakteristik Utama |
|---|---|---|
| [01_dead_wheel.md](01_dead_wheel.md) | Dead Wheel Odometry | Paling presisi, butuh roda tambahan yang nggak dipakai buat gerak |
| [02_motor_encoder.md](02_motor_encoder.md) | Motor Encoder Odometry | Paling sederhana (nggak ada part tambahan), tapi paling nggak akurat |
| [03_sensor_optik.md](03_sensor_optik.md) | Sensor Optik (Optical Tracking) | Kompak, nggak ada roda, "membaca" gerakan dari lantai langsung |
| [04_sensor_fusion_imu.md](04_sensor_fusion_imu.md) | Sensor Fusion dengan IMU | Bukan pengganti metode di atas, tapi lapisan tambahan buat perbaiki akurasi heading |

## Konsep Dasar yang Perlu Dipahami

1. **Slip (selip) adalah musuh utama odometry** — kalau roda yang dipakai buat menghitung jarak juga dipakai buat menggerakkan robot (roda drivetrain), roda itu bisa selip (spin di tempat tanpa benar-benar maju) terutama saat akselerasi/tabrakan, dan itu bikin perhitungan posisi meleset. Ini alasan utama kenapa "dead wheel" (roda yang nggak dipakai gerak, cuma buat ngukur) jadi populer.
2. **Drift** — kesalahan kecil yang terus menumpuk (accumulate) seiring waktu. Semakin lama match berjalan tanpa "reset" posisi (misalnya nabrak dinding lapangan untuk kalibrasi ulang), semakin besar kemungkinan posisi yang dihitung robot meleset dari posisi aslinya.
3. **Heading (arah hadap) sama pentingnya dengan posisi X/Y** — kalau perhitungan arah hadap robot sedikit saja salah, kesalahan itu akan makin membesar seiring jarak yang ditempuh (mirip orang jalan dengan kompas yang meleset sedikit, makin jauh jalan makin jauh dari tujuan).
4. **Odometry beda dengan localization berbasis vision (AprilTag/kamera)** — odometry murni menghitung dari gerakan robot sendiri (relatif ke titik awal), sedangkan vision-based localization "melihat" penanda tetap di lapangan buat tahu posisi absolut. Banyak tim menggabungkan keduanya: odometry buat gerakan halus/cepat, vision buat koreksi berkala.

## Cara Memilih

- **Berapa presisi yang dibutuhkan?** Kalau strategi autonomous butuh presisi tinggi (misalnya parkir tepat di titik kecil, atau menembak dari posisi presisi), dead wheel atau sensor optik jauh lebih andal dibanding motor encoder saja.
- **Berapa banyak ruang & anggaran yang tersedia?** Dead wheel butuh ruang fisik tambahan buat roda dan mekanisme pegasnya; sensor optik lebih kompak tapi harganya sebanding.
- **Seberapa sering robot mengalami tabrakan/dorongan dari robot lain?** Kalau strategi permainan melibatkan kontak fisik dengan robot lain, metode yang tahan terhadap slip (dead wheel, sensor optik) jauh lebih penting dibanding kalau robot jarang bersentuhan dengan robot lain.
