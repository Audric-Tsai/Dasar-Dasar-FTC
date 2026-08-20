# Panduan Build — Indexer + Flywheel (Continuous Feed)

Pendamping [03_indexer_flywheel.md](03_indexer_flywheel.md) (penjelasan kelebihan/kekurangan). Di sini fokus ke wheel yang cocok, tips build, dan link referensi. Ini nambahin sistem indexer ke flywheel tunggal ATAU ganda yang sudah dibangun duluan — bukan pengganti keduanya.

## Saran Wheel

Sama seperti flywheel tunggal/ganda (durometer keras) — pilihan wheel nggak berubah, yang berubah cuma cara game piece dimasukkan ke flywheel-nya.

## Tips Build

- Mulai dari flywheel yang SUDAH bekerja konsisten (tunggal atau ganda) sebelum menambahkan indexer — jangan bangun keduanya bersamaan, susah debug kalau dua sistem baru sekaligus.
- Indexer biasanya pakai servo (bukan motor DC) karena cuma butuh gerakan buka-tutup gate, bukan putaran kontinu.
- Kasih jeda (delay) yang cukup antara "indexer dorong game piece" dan "indexer dorong berikutnya" — jeda ini penting supaya RPM flywheel sempat pulih ke target sebelum game piece berikutnya masuk.
- Indexing yang bagus itu urusan software SAMA PENTINGNYA dengan mekanik — video referensi di bawah menunjukkan mekanisme indexing musim FTC DECODE yang bisa jadi contoh nyata.

## Link Panduan

- [REV Robotics — Flywheel Launcher](https://docs.revrobotics.com/ftc-kickoff-concepts/ultimate-goal-2020-2021/shooter)
- [REV Robotics — Programming Tips & Tricks (DECODE 2025-26)](https://docs.revrobotics.com/ftc-kickoff-concepts/decode-2025-26/programming-tips-and-tricks)
- [Game Manual 0 — Common Mechanisms](https://gm0.org/en/latest/docs/common-mechanisms/index.html)
- [FTC Tech Toolbox — Flywheels/Shooters (programming)](https://ftc-tech-toolbox.vercel.app/docs/Commonly%20Programmed%20Modules/shooter)

## Video YouTube

- [Insane Indexing Mechanism: FTC Decode](https://www.youtube.com/shorts/CeSpba4mcas) — contoh nyata indexer musim FTC DECODE
- [FTC Tips and Tricks: Shooting Mechanisms](https://www.youtube.com/watch?v=6OW-YfxOt1Y)
