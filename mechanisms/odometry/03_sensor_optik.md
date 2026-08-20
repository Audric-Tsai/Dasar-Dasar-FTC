# Sensor Optik (Optical Tracking)

## Cara Kerja

Sensor kecil yang dipasang menghadap ke lantai, memakai kamera resolusi rendah kecepatan tinggi (mirip sensor di dalam mouse komputer optik) buat "membaca" pola permukaan lantai berkali-kali per detik dan menghitung seberapa jauh & ke arah mana robot bergeser di antara dua pembacaan. Nggak ada roda yang menyentuh lantai sama sekali — semua perhitungan gerak dilakukan dari citra optik.

## Kelebihan

- **Sangat kompak** — cuma satu sensor kecil, nggak butuh roda, pegas, atau mekanisme mounting yang rumit seperti dead wheel.
- **Nggak ada bagian yang bisa selip** — karena bukan roda yang berputar, nggak ada konsep "selip" seperti dead wheel atau motor encoder; pembacaannya langsung dari gerakan relatif ke lantai.
- **Instalasi jauh lebih sederhana** — tinggal pasang satu sensor di posisi yang menghadap lantai dengan jarak yang benar, dibanding harus memasang 2-3 dead wheel dengan pegas dan trackwidth yang presisi.
- **Beberapa produk sudah menggabungkan IMU internal** — jadi satu sensor bisa langsung memberi data posisi DAN heading sekaligus, tanpa perlu sensor fusion terpisah.

## Kekurangan

- **Sensitif terhadap jarak & pencahayaan** — sensor perlu dipasang di ketinggian yang tepat dari lantai (biasanya dalam rentang beberapa milimeter) supaya bisa fokus membaca pola lantai dengan benar; terlalu tinggi/rendah bikin bacaan nggak akurat.
- **Bisa terganggu di permukaan yang sangat seragam/reflektif** — kalau lantai lapangan terlalu polos/mengkilap sehingga sensor kesulitan menemukan pola tekstur buat dilacak, akurasinya bisa menurun.
- **Cuma satu titik pembacaan** — beda dengan dead wheel yang bisa dipasang di beberapa titik buat saling mengoreksi, sensor optik biasanya cuma satu unit, jadi kalau sensornya kotor/tertutup debu, seluruh sistem odometry ikut terganggu.
- **Harganya sebanding dengan dead wheel** — meskipun secara part lebih sedikit, harga satu sensor optik biasanya nggak jauh beda dengan biaya total dead wheel pod, jadi bukan berarti otomatis lebih murah.

## Kapan Sebaiknya Dipakai

Kalau tim butuh solusi odometry presisi tinggi tapi ruang desain sangat terbatas buat pasang dead wheel, atau ingin instalasi yang jauh lebih sederhana dan cepat dibanding merakit mekanisme dead wheel dari nol.
