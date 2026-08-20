# Panduan Build — Catapult / Trebuchet

Pendamping [04_catapult.md](04_catapult.md) (penjelasan kelebihan/kekurangan). Di sini fokus ke tips build dan link referensi. Catapult nggak pakai wheel sama sekali — jadi nggak ada bagian "saran wheel" di sini.

## Tips Build

- Panjang lengan catapult berpengaruh langsung ke jarak lontar — makin panjang, makin jauh, tapi juga makin besar torsi yang dibutuhkan buat menggerakkannya.
- Kalau pakai servo (bukan pegas/karet), pastikan torsi servo cukup buat massa lengan + game piece di ujungnya — servo standar FTC sering nggak cukup kuat tanpa gearing tambahan.
- Kalau pakai pegas/karet elastis (surgical tubing, rubber band), servo cuma dipakai buat "melepas kunci" (release/trigger), bukan buat menggerakkan lengan secara langsung — ini biasanya menghasilkan lontaran yang lebih cepat dan konsisten dibanding servo langsung.
- Titik lepas (release point) game piece dari cup/lengan harus presisi dan konsisten — kalau berubah-ubah dikit saja, hasil lontaran bisa sangat berbeda tembakan demi tembakan.
- Sediakan ruang ayun yang cukup di dalam batas ukuran robot — ini sering jadi kendala desain utama buat catapult.

## Link Panduan

- [Instructables — Servo Powered Catapult](https://www.instructables.com/Servo-Powered-Catapult/)
- [Maker Pro — How to Make a DIY Catapult With Arduino](https://maker.pro/arduino/projects/arduino-servo-catapult)
- [Game Manual 0 — Common Mechanisms](https://gm0.org/en/latest/docs/common-mechanisms/index.html)

## Video YouTube

- [Functional ChooChoo Catapult: FTC Decode](https://www.youtube.com/shorts/7JoVEDv8vUE) — contoh catapult musim FTC DECODE
- [Reliable Shooter With Servo Launcher — Team 24909 StarLight](https://www.youtube.com/shorts/9El5UcekiR0) — servo launcher FTC DECODE, dibuat dalam "Robot in 30 Hours"
