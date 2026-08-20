# Claw / Cakar (Gripper) Intake

## Cara Kerja

Dua "jari" atau lebih yang digerakkan oleh satu atau dua servo, membuka dan menutup untuk menjepit game piece secara langsung. Biasanya dipasang di ujung lengan (arm) atau slide linear supaya bisa mencapai posisi ambil yang tepat. Ini bukan intake yang "menarik" seperti roller, tapi lebih ke "menjemput dan menjepit" — jadi lebih tepat disebut end-effector, tapi sering dikategorikan sebagai jenis intake juga.

## Kelebihan

- **Presisi tinggi** — cengkeraman langsung memastikan game piece dipegang persis seperti yang diinginkan, orientasinya bisa dikontrol.
- **Cocok untuk bentuk yang rumit atau rapuh** — nggak ada tekanan gesekan berulang seperti roller, jadi lebih aman untuk game piece yang gampang rusak.
- **Hemat daya saat diam** — servo cuma butuh daya saat bergerak membuka/menutup, nggak seperti motor DC yang terus berputar.
- **Bisa menahan game piece dengan aman** meski robot bergerak cepat atau menabrak sesuatu, karena cengkeraman mekanis.

## Kekurangan

- **Lambat** — perlu waktu untuk memposisikan lengan/slide, membuka cakar, menjepit, lalu menarik lagi. Cycle time jauh lebih lambat dibanding roller intake.
- **Butuh presisi posisi robot** — game piece harus ada tepat di jangkauan cakar; robot nggak bisa asal menyodok seperti roller intake.
- **Servo terbatas torsi dan kecepatan** — cengkeraman yang terlalu kuat bisa merusak game piece, terlalu lemah bisa terlepas saat bergerak.
- **Butuh sistem tambahan (arm/slide)** — cakar sendiri nggak berguna tanpa mekanisme untuk mengantarkannya ke posisi game piece, jadi kompleksitas total sistem lebih tinggi.

## Kapan Sebaiknya Dipakai

Kalau game piece butuh orientasi yang presisi saat diambil atau diletakkan (misalnya disusun di suatu tempat), atau kalau jumlah game piece per match sedikit sehingga cycle time yang lambat bukan masalah besar.
