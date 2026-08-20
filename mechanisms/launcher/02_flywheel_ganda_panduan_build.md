# Panduan Build — Flywheel Ganda (Double Flywheel)

Pendamping [02_flywheel_ganda.md](02_flywheel_ganda.md) (penjelasan kelebihan/kekurangan). Di sini fokus ke wheel yang cocok, tips build, dan link referensi.

## Saran Wheel

Pakai dua wheel yang IDENTIK (durometer & diameter sama persis) di kedua sisi supaya dorongannya simetris:
- REV Traction Wheel 90mm (durometer medium-hard) — cocok dipasang sepasang
- goBILDA GripForce (40A Durometer) sebagai opsi upgrade kalau butuh lebih keras/lebih jauh

## Tips Build

- Dua motor HARUS disinkronkan RPM-nya lewat kode (bukan cuma dikasih power yang sama) — encoder di masing-masing motor dipakai buat closed-loop control supaya kecepatan dua wheel benar-benar sama.
- Jarak antar dua flywheel (gap) harus presisi sesuai diameter game piece — kurang presisi bisa bikin macet atau lontaran lemah sebelah.
- REV sendiri menjelaskan pola umum: motor pertama mempercepat game piece ke kecepatan tertentu, motor kedua mempercepat lagi ke kecepatan akhir yang lebih tinggi — bukan cuma dua motor identik yang jalan bareng dari awal.
- Pakai kontrol PID di kedua motor secara independen, baru disamakan target RPM-nya.

## Link Panduan

- [REV Robotics — Flywheel Launcher (penjelasan desain double flywheel)](https://docs.revrobotics.com/ftc-kickoff-concepts/ultimate-goal-2020-2021/shooter)
- [Game Manual 0 — Common Mechanisms](https://gm0.org/en/latest/docs/common-mechanisms/index.html)
- [YETI Robotics Wiki — Shooter Designs](https://wiki.yetirobotics.org/books/design-process/page/shooter-designs)
- [FTC Tech Toolbox — Flywheels/Shooters (programming)](https://ftc-tech-toolbox.vercel.app/docs/Commonly%20Programmed%20Modules/shooter)

## Video YouTube

- [Shooter Part 1 - FTC Ultimate Goal 2020-2021 Prototyping](https://www.youtube.com/watch?v=eHVtErHj9lg)
- [FTC Tips and Tricks: Shooting Mechanisms](https://www.youtube.com/watch?v=6OW-YfxOt1Y)
