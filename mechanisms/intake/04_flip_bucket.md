# Flip-Bucket (Servo) Intake

## Cara Kerja

Sebuah wadah kecil (bucket) atau lengan bergerak yang digerakkan servo, berayun dari posisi rendah (dekat lantai/sumber game piece) ke posisi tinggi (tempat penyimpanan atau outtake). Game piece "ditangkap" saat bucket di posisi bawah, lalu terbawa saat bucket berputar/flip ke atas dan dijatuhkan atau dituang keluar.

## Kelebihan

- **Sekaligus jadi penyimpanan sementara** — nggak perlu belt atau mekanisme transport terpisah, bucket-nya sendiri yang membawa game piece.
- **Hemat part dan berat** — cuma butuh satu servo dan satu bucket, jauh lebih ringan dibanding sistem roller + belt.
- **Gerakan bisa presisi dan berulang** — servo posisinya bisa diatur pasti (misalnya lewat PWM/posisi), jadi gerakan flip konsisten setiap saat.
- **Nggak butuh permukaan gesek khusus** — game piece cuma perlu "masuk" ke bucket, jadi lebih toleran terhadap bentuk yang licin atau nggak beraturan (asal muat).

## Kekurangan

- **Lambat per siklus** — servo perlu waktu untuk flip naik-turun secara penuh sebelum bisa ambil game piece berikutnya, nggak bisa "terus menerus" seperti roller.
- **Kapasitas terbatas** — biasanya cuma bisa bawa satu (atau sedikit) game piece per siklus, sesuai ukuran bucket.
- **Perlu timing yang pas** — robot harus berada di posisi yang benar saat bucket ada di bawah, kalau nggak, game piece bisa terlewat.
- **Servo bisa kewalahan kalau beban berat** — untuk game piece yang berat, torsi servo standar mungkin nggak cukup, perlu servo yang lebih kuat atau gearing tambahan.

## Kapan Sebaiknya Dipakai

Kalau tim butuh solusi ringan dan sederhana untuk memindahkan game piece dari titik ambil ke titik simpan/keluar, dan cycle time cepat bukan prioritas utama dibanding kesederhanaan mekanis dan bobot robot yang ringan.
