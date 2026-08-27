package org.firstinspires.ftc.teamcode;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ============================================================================
 *   AUTO UNIT TESTING — File Ke-20 Ini SATU-SATUNYA yang Nggak Pernah ke Robot
 * ============================================================================
 *
 *   BERHENTI DULU SEBELUM BACA LEBIH JAUH — file ini BEDA TOTAL dari
 *   Auto01-19:
 *
 *     - TIDAK ADA @Autonomous. TIDAK extends LinearOpMode. TIDAK
 *       PERNAH muncul di daftar OpMode Driver Station. TIDAK PERNAH
 *       dikirim/deploy ke robot SAMA SEKALI.
 *     - Lokasinya juga beda: TeamCode/src/TEST/java/..., BUKAN
 *       TeamCode/src/MAIN/java/... seperti semua Auto0N lain. Ini
 *       "source set" terpisah — Gradle (sistem build project ini)
 *       tahu file di src/test itu cuma buat DITES DI KOMPUTER, dan
 *       nggak pernah diikutkan waktu bikin APK yang di-install ke
 *       Control Hub.
 *     - Cara MENJALANKANNYA juga beda: bukan tekan PLAY di Driver
 *       Station, tapi jalankan perintah ini dari terminal:
 *
 *           ./gradlew :TeamCode:testDebugUnitTest
 *
 *       Hasilnya langsung muncul di terminal dalam hitungan DETIK —
 *       nggak perlu nyalain robot, nggak perlu Control Hub, nggak
 *       perlu WiFi, nggak perlu APAPUN selain laptop.
 *
 *   KENAPA INI BISA DILAKUKAN — DAN KENAPA Auto01-19 NGGAK BISA:
 *   Semua OpMode di seri ini BUTUH hardware asli buat dijalankan
 *   (motorKiri.setPower() nggak ada artinya tanpa motor beneran
 *   nyala). Tapi nggak SEMUA kode di seri ini kayak gitu — coba
 *   ingat-ingat bedakanSudut(), inciKeTick(), rumus kinematika
 *   mecanum di Auto13, atau rumus posisi profil trapesium di Auto17.
 *   Semua itu MURNI MATEMATIKA: angka masuk, angka keluar, NGGAK ADA
 *   satu baris pun yang menyentuh motor/sensor/telemetry.
 *
 *   Fungsi matematika MURNI kayak gitu bisa DITES OTOMATIS, berkali-
 *   kali, dalam hitungan milidetik, TANPA robot sama sekali — asal
 *   dia bisa "dijangkau" dari luar class OpMode-nya. Itu sebabnya
 *   Subsistem_Matematika.java (di src/MAIN, satu paket dengan file
 *   ini) ada: rumus yang sama dari Auto02/04/05/07/08/13/17/19,
 *   dipindah jadi method PUBLIC STATIC, dites di sini.
 *
 *   INI JUGA JAWABAN BUAT PERTANYAAN YANG (MUNGKIN) BELUM PERNAH
 *   KAMU TANYAKAN: "gimana caranya YAKIN rumus PID/kinematika/
 *   odometri di kode ini BENAR, SEBELUM nyoba di robot beneran?"
 *   Unit test itu jawabannya — cek matematikanya di komputer DULU,
 *   baru percaya robotnya waktu dites fisik. Kalau ada satu tanda
 *   plus/minus ketuker di rumus mecanum, tes ini akan GAGAL dalam
 *   hitungan detik — jauh lebih cepat (dan jauh lebih aman) daripada
 *   nemuin itu waktu robot beneran nyelonong ke arah yang salah.
 *
 * ============================================================================
 */
public class Auto20_UnitTesting {

    // ========================================================================
    //   bedakanSudutDerajat() — dari Auto02/04/05/08/19
    // ========================================================================

    @Test
    public void bedakanSudutDerajat_kasusBiasa() {
        // Target 30 derajat, sekarang 10 derajat -> masih perlu 20 derajat lagi.
        assertEquals(20.0, Subsistem_Matematika.bedakanSudutDerajat(30, 10), 0.0001);
    }

    @Test
    public void bedakanSudutDerajat_lewatBatas180_kasusYangDIRANCANGDicegah() {
        // Ini INTI kenapa fungsi ini ada (lihat javadoc Auto02 metode 5).
        // target - sekarang = 179 - (-179) = 358 mentah, tapi robot
        // secara fisik cuma perlu geser 2 derajat ke arah SEBALIKNYA
        // (358 derajat satu arah == 2 derajat arah satunya, di
        // lingkaran). Fungsi ini membungkusnya jadi -2, bukan 358.
        assertEquals(-2.0, Subsistem_Matematika.bedakanSudutDerajat(179, -179), 0.0001);
    }

    @Test
    public void bedakanSudutDerajat_arahSebaliknya() {
        // Kebalikannya: target -179, sekarang 179 -> dibungkus jadi +2.
        assertEquals(2.0, Subsistem_Matematika.bedakanSudutDerajat(-179, 179), 0.0001);
    }

    @Test
    public void bedakanSudutRadian_lewatBatasPi() {
        double hampirPi = Math.PI - 0.01;
        double target = -hampirPi;
        // Sama seperti tes derajat di atas, versi radian.
        double hasil = Subsistem_Matematika.bedakanSudutRadian(target, hampirPi);
        assertEquals(0.02, hasil, 0.0001);
    }

    // ========================================================================
    //   inciKeTick() / tickKeInci() — dari Auto01
    // ========================================================================

    @Test
    public void inciKeTick_tickKeInci_roundTrip() {
        double diameterRoda = 3.54;
        double tickPerPutaran = 560.0;

        int tick = Subsistem_Matematika.inciKeTick(24.0, diameterRoda, tickPerPutaran);
        double inciKembali = Subsistem_Matematika.tickKeInci(tick, diameterRoda, tickPerPutaran);

        // Bolak-balik (inci -> tick -> inci) harusnya dapat angka yang
        // SANGAT dekat ke aslinya. Pembulatan int di inciKeTick() bikin
        // ini nggak akan PERSIS sama, makanya delta-nya longgar dikit.
        assertEquals(24.0, inciKembali, 0.05);
    }

    // ========================================================================
    //   hitungPowerMecanum() — dari Auto13
    // ========================================================================

    @Test
    public void mecanum_majuSaja() {
        double[] power = Subsistem_Matematika.hitungPowerMecanum(0.5, 0, 0);
        // Maju murni: SEMUA motor power sama, nggak ada yang beda arah.
        assertEquals(0.5, power[0], 0.0001); // depanKiri
        assertEquals(0.5, power[1], 0.0001); // depanKanan
        assertEquals(0.5, power[2], 0.0001); // belakangKiri
        assertEquals(0.5, power[3], 0.0001); // belakangKanan
    }

    @Test
    public void mecanum_geserMurni() {
        double[] power = Subsistem_Matematika.hitungPowerMecanum(0, 0.5, 0);
        // Geser ke kanan: depanKiri & belakangKanan POSITIF,
        // depanKanan & belakangKiri NEGATIF. Lihat javadoc Auto13
        // metode 2 buat penjelasan lengkap kenapa pola tanda ini
        // yang menghasilkan geseran, bukan putaran.
        assertEquals(0.5, power[0], 0.0001);  // depanKiri
        assertEquals(-0.5, power[1], 0.0001); // depanKanan
        assertEquals(-0.5, power[2], 0.0001); // belakangKiri
        assertEquals(0.5, power[3], 0.0001);  // belakangKanan
    }

    @Test
    public void mecanum_normalisasi_tidakMelebihiSatu() {
        // axial=0.8, lateral=0.8 -> depanKiri mentah = 1.6, JAUH di
        // atas 1.0. Setelah normalisasi, TIDAK ADA satu pun motor
        // yang power-nya melebihi 1.0 (lihat javadoc Auto13 metode 3
        // soal kenapa normalisasi harus MEMBAGI SEMUA motor, bukan
        // cuma motong yang kelebihan).
        double[] power = Subsistem_Matematika.hitungPowerMecanum(0.8, 0.8, 0);
        for (double p : power) {
            assertTrue("Power " + p + " melebihi 1.0", Math.abs(p) <= 1.0001);
        }
    }

    @Test
    public void mecanum_normalisasi_menjagaPerbandingan() {
        // Sebelum normalisasi: depanKiri mentah = 1.6, depanKanan
        // mentah = 0.0 (0.8 - 0.8 - 0). Rasio depanKiri:depanKanan
        // aslinya 1.6:0. Setelah dibagi 1.6, harusnya jadi 1.0:0 —
        // PERBANDINGANNYA harus tetap sama, cuma skalanya mengecil.
        double[] power = Subsistem_Matematika.hitungPowerMecanum(0.8, 0.8, 0);
        assertEquals(1.0, power[0], 0.0001);
        assertEquals(0.0, power[1], 0.0001);
    }

    // ========================================================================
    //   Profil Trapesium — dari Auto17
    // ========================================================================

    @Test
    public void profilTrapesium_titikAwal_diamDanDiPosisiNol() {
        double jarak = 48, jelajah = 20, percepatan = 40;
        assertEquals(0.0, Subsistem_Matematika.posisiProfil(0, jarak, jelajah, percepatan), 0.0001);
        assertEquals(0.0, Subsistem_Matematika.kecepatanProfil(0, jarak, jelajah, percepatan), 0.0001);
    }

    @Test
    public void profilTrapesium_titikAkhir_diamDanSampaiTarget() {
        double jarak = 48, jelajah = 20, percepatan = 40;
        double waktuTotal = Subsistem_Matematika.waktuTotalProfil(jarak, jelajah, percepatan);

        assertEquals(jarak, Subsistem_Matematika.posisiProfil(waktuTotal, jarak, jelajah, percepatan), 0.01);
        assertEquals(0.0, Subsistem_Matematika.kecepatanProfil(waktuTotal, jarak, jelajah, percepatan), 0.01);
    }

    @Test
    public void profilTrapesium_tengahJelajah_kecepatanPenuh() {
        // Jarak jauh (48 inci) dengan percepatan tinggi (40) harusnya
        // SEMPAT mencapai kecepatan jelajah penuh sebelum melambat
        // lagi. Cek TEPAT DI TENGAH waktu total, kecepatan planned
        // seharusnya persis di kecepatan jelajah.
        double jarak = 48, jelajah = 20, percepatan = 40;
        double waktuTotal = Subsistem_Matematika.waktuTotalProfil(jarak, jelajah, percepatan);

        double kecepatanDiTengah = Subsistem_Matematika.kecepatanProfil(waktuTotal / 2.0, jarak, jelajah, percepatan);
        assertEquals(jelajah, kecepatanDiTengah, 0.01);
    }

    @Test
    public void profilTrapesium_jarakPendek_jadiSegitiga_nggakPernahCapaiJelajah() {
        // Jarak SANGAT pendek (2 inci) — nggak akan pernah sempat
        // capai kecepatan jelajah 20 inci/detik sebelum harus mulai
        // melambat lagi (segitiga, bukan trapesium). Kecepatan
        // PUNCAK yang beneran dicapai harus LEBIH KECIL dari jelajah.
        double jarak = 2, jelajah = 20, percepatan = 40;
        double waktuTotal = Subsistem_Matematika.waktuTotalProfil(jarak, jelajah, percepatan);

        double kecepatanPuncakAsli = Subsistem_Matematika.kecepatanProfil(waktuTotal / 2.0, jarak, jelajah, percepatan);
        assertTrue("Kecepatan puncak (" + kecepatanPuncakAsli + ") harusnya di bawah jelajah (" + jelajah + ") buat jarak pendek",
                kecepatanPuncakAsli < jelajah);
    }
}

/*
 * ============================================================================
 *   LATIHAN — kerjakan berurutan
 * ============================================================================
 *
 *  PERCOBAAN 1 — Jalankan semua tes
 *    a. Dari terminal, di root folder project, jalankan:
 *         ./gradlew :TeamCode:testDebugUnitTest
 *    b. Semua tes harusnya "PASSED" berwarna hijau (atau tanda
 *       centang, tergantung terminal). Nggak ada robot yang menyala.
 *
 *  PERCOBAAN 2 — Buktikan tes beneran mendeteksi bug
 *    a. Di Subsistem_Matematika.java, tukar tanda + dan - di
 *       hitungPowerMecanum() (misalnya depanKanan jadi
 *       "axial + lateral + putar", salah sengaja)
 *    b. Jalankan ulang tesnya. Tes MANA yang gagal? Baca pesan
 *       errornya — apa dia langsung nunjuk ke baris yang salah?
 *    c. Kembalikan kodenya, jalankan lagi, pastikan semua PASSED
 *       lagi sebelum lanjut
 *
 *  PERCOBAAN 3 — Tambah tes buat kasus yang belum dicek
 *    a. Tulis tes baru: mecanum_putarMurni() — cek axial=0, lateral=0,
 *       putar=0.5, pastikan tanda power tiap motor sesuai rumus
 *       (depanKiri & belakangKiri satu arah, depanKanan & belakangKanan
 *       arah berlawanan)
 *    b. Tulis tes: bedakanSudutDerajat_samaPersis() — target dan
 *       sekarang angkanya SAMA, hasilnya harus 0
 *
 *  TANTANGAN
 *    a. Pindahkan getSudutLengan() dari Auto18_PIDF jadi method
 *       static baru di Subsistem_Matematika (butuh parameter
 *       tambahan: tickSekarang, sudutAwal, tickPerPutaranLengan),
 *       lalu tulis tes yang memverifikasi konversi tick->derajatnya
 *    b. Cek: SEMUA fungsi yang baru kamu tulis di seri Auto0N ini —
 *       mana lagi yang PURE (nggak nyentuh hardware) dan bisa
 *       dipindah ke Subsistem_Matematika supaya ikutan bisa dites?
 * ============================================================================
 */
