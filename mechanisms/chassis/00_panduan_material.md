# Panduan Material Chassis/Rangka FTC

Dokumen ini membandingkan material yang umum dipakai untuk chassis (rangka utama) dan panel-panel robot FTC. Fokusnya di tiga hal yang paling menentukan keputusan tim: **biaya**, **massa/berat**, dan **di mana belinya**.

## Ringkasan Perbandingan

| File | Material | Kelebihan Utama | Kekurangan Utama |
|---|---|---|---|
| [01_aluminium.md](01_aluminium.md) | Aluminium 6061-T6 (extrusion/plat) | Kuat, mudah dikerjakan, standar industri FTC | Lebih berat dari carbon fiber |
| [02_carbon_fiber.md](02_carbon_fiber.md) | Carbon Fiber (sheet/tube) | Sangat ringan, sangat kaku | MAHAL, sulit dikerjakan, rapuh kalau kena beban titik |
| [03_plastik_cetak_3d.md](03_plastik_cetak_3d.md) | Plastik cetak 3D (PETG/ABS/Nylon) | Bisa cetak bentuk custom apa saja, murah | Kurang kuat untuk struktur utama, butuh printer |
| [04_polikarbonat.md](04_polikarbonat.md) | Polikarbonat / Lexan | Ringan, tahan benturan, transparan opsional | Bukan untuk struktur penahan beban utama |

## Perbandingan Cepat: Biaya & Massa

```text
Material                     Densitas       Perkiraan Biaya Bahan Baku      Catatan
--------------------------------------------------------------------------------------------------------------
Aluminium 6061-T6             2.70 g/cm3     ~US$3.5-6 / kg                  Harga plat/batangan umum, belum termasuk cutting
Carbon Fiber (sheet, prepreg) 1.55-1.60 g/cm3 ~US$65-140+ / kg                Bisa 15-30x lebih mahal dari aluminium per kg
PETG (cetak 3D)               1.27 g/cm3     ~US$20-30 / kg (filamen)        Butuh printer 3D + waktu cetak
ABS (cetak 3D)                1.04 g/cm3     ~US$18-25 / kg (filamen)        Sedikit lebih ringan dari PETG, lebih rapuh di suhu dingin
Polikarbonat (lembaran)       1.20 g/cm3     ~US$8-15 / kg                   Umumnya dijual per lembar (mis. 2mm/3mm), bukan per kg
```

Angka biaya di atas adalah estimasi harga bahan mentah global (bisa berubah, dan belum termasuk ongkos potong/kirim). Untuk harga aktual di Indonesia, cek `parts/Parts to Buy/chassis.md` di repo FTC-CK — di sana ada rekomendasi konkret dan link belinya.

## Cara Memilih

1. **Berapa banyak beban yang harus ditahan?** Kalau itu rangka utama (base robot, tempat motor drivetrain menempel), aluminium tetap pilihan paling aman. Carbon fiber gampang retak kalau kena beban titik dari baut/sekrup tanpa reinforcement.
2. **Seberapa penting berat robot?** FTC punya batas berat maksimum — kalau robot mepet limit dan butuh space buat mekanisme lain, carbon fiber di bagian non-struktural (penutup, lengan panjang) bisa menghemat banyak berat.
3. **Berapa budget yang tersedia?** Carbon fiber jauh lebih mahal per kilogram — biasanya cuma dipakai di bagian kecil dan kritis (misalnya lengan linear slide tambahan), bukan seluruh chassis.
4. **Apakah tim punya printer 3D?** Kalau ada, plastik cetak 3D sangat murah untuk bracket custom, dudukan sensor, atau part kecil yang bentuknya rumit — tapi jangan dipakai untuk bagian yang menahan beban besar seperti dudukan motor drivetrain.
