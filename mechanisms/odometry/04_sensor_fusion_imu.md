# Sensor Fusion dengan IMU

## Cara Kerja

Ini BUKAN metode odometry yang berdiri sendiri seperti tiga file sebelumnya — ini lapisan tambahan yang menggabungkan (fusion) data dari IMU (Inertial Measurement Unit, sensor yang mendeteksi rotasi & percepatan) dengan data dari metode odometry lain (biasanya dead wheel) buat menghasilkan perhitungan posisi & heading yang lebih akurat dan lebih cepat dibanding data odometry mentah saja.

## Kelebihan

- **Heading jauh lebih akurat** — IMU sangat andal buat mengukur perubahan arah hadap (rotasi), seringkali lebih presisi dibanding menghitung heading cuma dari selisih dua dead wheel yang sejajar.
- **Mengurangi jumlah dead wheel yang dibutuhkan** — sistem yang biasanya butuh 3 dead wheel (2 buat heading + 1 buat strafe) bisa disederhanakan jadi 2 dead wheel aja (1 forward + 1 strafe), karena IMU yang menangani perhitungan heading.
- **Update rate lebih cepat** — sensor coprocessor khusus buat sensor fusion biasanya bisa menghitung posisi ribuan kali per detik, jauh lebih cepat dibanding menghitung semuanya lewat kode utama robot di Control Hub (yang harus berbagi waktu proses dengan tugas lain).
- **Mengurangi beban komputasi Control Hub** — kalau sensor fusion dilakukan di coprocessor terpisah (bukan dihitung software robot), Control Hub jadi lebih lega buat memproses hal lain (vision, kontrol mekanisme, dll).

## Kekurangan

- **Tetap butuh sumber data dasar (dead wheel/encoder)** — sensor fusion nggak menggantikan kebutuhan dead wheel, cuma memperbaiki cara datanya digabungkan. Jadi kekurangan dead wheel (butuh ruang fisik, rawan benturan) tetap ada.
- **IMU bisa drift juga** — meskipun IMU bagus buat perubahan jangka pendek, dia juga bisa mengalami drift kalau nggak dikalibrasi ulang secara berkala.
- **Kompleksitas setup lebih tinggi** — perlu memahami cara kalibrasi orientasi IMU dengan benar (mirip yang dibahas di `AutoMajuSimple` metode 5) supaya arah rotasi terbaca sesuai orientasi fisik robot.
- **Kalau pakai coprocessor khusus, jadi ketergantungan sama satu produk tertentu** — beda dengan metode DIY (encoder + kode sendiri) yang lebih fleksibel diganti-ganti komponennya.

## Kapan Sebaiknya Dipakai

Hampir selalu direkomendasikan sebagai pelengkap dead wheel odometry buat tim yang serius soal presisi autonomous — bukan pilihan yang berdiri sendiri, tapi peningkatan dari sistem dead wheel dasar yang sudah berjalan.
