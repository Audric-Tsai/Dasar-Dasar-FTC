package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * ============================================================================
 *   AUTO SENSOR WARNA — Membaca Warna itu Nggak Sesederhana Kelihatannya
 * ============================================================================
 *
 *   Sensor warna nembak lampu (LED) sendiri ke permukaan di depannya,
 *   lalu ukur berapa banyak cahaya MERAH, HIJAU, dan BIRU yang
 *   MEMANTUL balik. Kedengarannya gampang: benda merah mantulin
 *   banyak merah, jadi tinggal cek "merah lebih tinggi dari biru?"
 *
 *   Masalahnya: cahaya yang memantul itu jumlahnya BUKAN CUMA
 *   tergantung warna bendanya. Dia juga tergantung SEBERAPA DEKAT
 *   bendanya (makin jauh, makin sedikit cahaya yang mantul balik —
 *   sama seperti senter yang keliatan lebih redup dari jauh), dan
 *   SEBERAPA TERANG cahaya ruangan di sekitarnya. Benda merah yang
 *   sama bisa kasih angka RGB yang JAUH BEDA di jarak yang beda, atau
 *   di lapangan kompetisi yang pencahayaannya beda dari ruang latihan.
 *
 *   Ini masalah yang SAMA jenisnya dengan Auto01 metode 1 (kecepatan
 *   motor berubah-ubah tergantung tegangan baterai) — cuma sekarang
 *   yang "goyang" itu bacaan sensor, bukan kecepatan motor. Solusinya
 *   juga mirip filosofinya: cari ukuran yang nggak ikut goyang
 *   walau kondisi di sekitarnya berubah.
 *
 *   FAKTA MENARIK:
 *   Banyak sensor warna (termasuk REV Color Sensor V3) sebenarnya
 *   SATU perangkat fisik yang menyediakan DUA kemampuan sekaligus:
 *   NormalizedColorSensor (warna) DAN DistanceSensor (jarak, dari
 *   Auto10) — kode di bawah nunjukkin cara cek itu.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive -> sama Auto01, dipakai metode 4 saja
 *     sensor_warna             -> NormalizedColorSensor, hadap ke
 *                                 bawah/depan, DEKAT ke permukaan
 *                                 (beberapa cm — sensor ini bukan
 *                                 buat jarak jauh)
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Sensor Warna (Belajar)", group = "Belajar")
public class Auto11_SensorWarna extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    /** Batas kasar buat metode 2 (naif) — lihat kenapa ini bermasalah di javadoc-nya. */
    private static final float BATAS_RGB_NAIF = 0.10f;

    /**
     * RENTANG HUE (DERAJAT, 0-360) — dipakai metode 3
     * Hue itu "warna murni"-nya, independen dari terang/gelap.
     * UKUR SENDIRI warna game element robotmu, jangan asal salin.
     */
    private static final float HUE_MERAH_MAKS = 30f;   // merah ada di sekitar 0 ATAU 360
    private static final float HUE_MERAH_MIN  = 330f;
    private static final float HUE_BIRU_MIN   = 180f;
    private static final float HUE_BIRU_MAKS  = 270f;

    /**
     * SATURASI MINIMUM — dipakai metode 3
     * Hue itu NGGAK PUNYA ARTI kalau bendanya abu-abu/putih/hitam
     * (saturasi rendah = "pudar"). Kalau saturasi di bawah ini,
     * jangan percaya hue-nya sama sekali.
     */
    private static final float SATURASI_MINIMUM = 0.4f;

    private static final double KECEPATAN = 0.3;
    private static final double WAKTU_GERAK_DETIK = 1.0;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private NormalizedColorSensor sensorWarna;
    private DcMotor motorKiri;
    private DcMotor motorKanan;

    // ========================================================================
    //   BAGIAN 3 — PROGRAM UTAMA
    // ========================================================================

    @Override
    public void runOpMode() {

        sensorWarna = hardwareMap.get(NormalizedColorSensor.class, "sensor_warna");

        motorKiri  = hardwareMap.get(DcMotor.class, "left_drive");
        motorKanan = hardwareMap.get(DcMotor.class, "right_drive");
        motorKiri.setDirection(DcMotor.Direction.REVERSE);
        motorKanan.setDirection(DcMotor.Direction.FORWARD);
        motorKiri.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorKanan.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.addLine("Dekatkan benda MERAH atau BIRU ke sensor.");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_BacaRGB();
        //metode2_DeteksiNaif();
        //metode3_DeteksiHue();
        //metode4_KeputusanOtonom();
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(3000);
    }

    // ========================================================================
    //   METODE 1 — BACA RGB MENTAH
    // ========================================================================
    /**
     * IDENYA:
     *   Tampilkan angka merah/hijau/biru mentah (0.0 - 1.0, sudah
     *   dinormalisasi ke rentang itu oleh SDK). Belum ada keputusan
     *   "ini warna apa" sama sekali.
     *
     * COBA INI:
     *   Dekatkan benda merah, catat angkanya. Jauhkan pelan-pelan
     *   (masih dalam jangkauan sensor), lihat SEMUA angka (merah,
     *   hijau, biru) ikut MENGECIL bersama-sama — bukan cuma satu
     *   angka yang berubah. Itu bukti klaim di banner atas: jarak
     *   mempengaruhi BESARNYA sinyal, bukan cuma warna yang
     *   terdeteksi.
     */
    private void metode1_BacaRGB() {
        while (opModeIsActive()) {

            NormalizedRGBA warna = sensorWarna.getNormalizedColors();

            telemetry.addData("METODE", "1 — RGB Mentah");
            telemetry.addData("Merah", "%.3f", warna.red);
            telemetry.addData("Hijau", "%.3f", warna.green);
            telemetry.addData("Biru", "%.3f", warna.blue);

            if (sensorWarna instanceof DistanceSensor) {
                double jarak = ((DistanceSensor) sensorWarna).getDistance(DistanceUnit.CM);
                telemetry.addData("Jarak (sensor yang sama)", "%.1f cm", jarak);
            }

            telemetry.addLine();
            telemetry.addLine("Dekatkan lalu jauhkan benda yang SAMA — semua angka");
            telemetry.addLine("ikut berubah bersama, bukan cuma satu warna.");
            telemetry.update();
        }
    }

    // ========================================================================
    //   METODE 2 — DETEKSI NAIF (BATAS TETAP)
    // ========================================================================
    /**
     * IDENYA:
     *   Bandingkan angka merah vs biru MENTAH langsung. Kalau merah
     *   jauh lebih tinggi -> "MERAH". Kalau biru jauh lebih tinggi
     *   -> "BIRU". Kalau nggak ada yang jelas menang, atau dua-duanya
     *   di bawah BATAS_RGB_NAIF -> "TIDAK DIKETAHUI".
     *
     * KENAPA INI RAPUH:
     *   BATAS_RGB_NAIF itu angka TETAP. Tapi lihat metode 1 —
     *   besarnya bacaan berubah drastis tergantung JARAK dan
     *   PENCAHAYAAN. Benda merah yang dites deket lampu terang bisa
     *   kasih merah=0.35. Benda merah YANG SAMA, dites lebih jauh
     *   atau di ruangan lebih gelap, bisa kasih merah=0.08 — DI
     *   BAWAH BATAS_RGB_NAIF, jadi keklasifikasi "TIDAK DIKETAHUI"
     *   padahal warnanya sama sekali nggak berubah.
     *
     * BUKTIKAN SENDIRI:
     *   Tes benda merah di jarak deket (harusnya kebaca MERAH), lalu
     *   di jarak agak jauh (masih dalam jangkauan sensor). Di titik
     *   mana dia berubah jadi TIDAK DIKETAHUI, padahal bendanya sama?
     */
    private void metode2_DeteksiNaif() {
        while (opModeIsActive()) {

            NormalizedRGBA warna = sensorWarna.getNormalizedColors();
            String hasil;

            if (warna.red > BATAS_RGB_NAIF && warna.red > warna.blue * 1.2f) {
                hasil = "MERAH";
            } else if (warna.blue > BATAS_RGB_NAIF && warna.blue > warna.red * 1.2f) {
                hasil = "BIRU";
            } else {
                hasil = "TIDAK DIKETAHUI";
            }

            telemetry.addData("METODE", "2 — Deteksi Naif");
            telemetry.addData("Merah/Biru mentah", "%.3f / %.3f", warna.red, warna.blue);
            telemetry.addData("Hasil", hasil);
            telemetry.update();
        }
    }

    // ========================================================================
    //   METODE 3 — DETEKSI PAKAI HUE (LEBIH TAHAN JARAK & CAHAYA)
    // ========================================================================
    /**
     * IDENYA:
     *   Ubah RGB jadi HSV (Hue, Saturation, Value) pakai fungsi
     *   bawaan Android: Color.RGBToHSV(). Lalu klasifikasi pakai
     *   HUE-nya saja, bukan RGB mentah.
     *
     * KENAPA HUE LEBIH TAHAN BANTING:
     *   Bayangkan lampu senter merah — deket sangat terang, jauh
     *   agak redup. RGB mentahnya (merah, hijau, biru) berubah besar-
     *   kecilnya waktu jaraknya berubah. Tapi PERBANDINGAN antar
     *   ketiganya (proporsi merah dibanding hijau dibanding biru)
     *   tetap kurang-lebih SAMA — sama-sama "dominan merah" entah dia
     *   terang atau redup. Hue itu SECARA MATEMATIS dihitung dari
     *   PERBANDINGAN itu, bukan dari besar mentahnya — makanya dia
     *   jauh lebih stabil waktu jarak/cahaya berubah.
     *
     *   Value (kecerahan) di HSV itu yang MENAMPUNG perubahan
     *   terang-gelap — hue nyaris nggak kena imbasnya.
     *
     * KENAPA ADA CEK SATURASI:
     *   Kalau permukaannya abu-abu, putih, atau hitam, dia nggak
     *   "condong" ke warna manapun — secara matematis hue-nya jadi
     *   ANGKA ACAK/NGGAK BERARTI (saturasi mendekati 0 bikin
     *   perhitungan hue jadi nggak stabil). Makanya HARUS dicek
     *   saturasi dulu sebelum percaya hue-nya.
     */
    private void metode3_DeteksiHue() {
        while (opModeIsActive()) {

            String hasil = bacaWarna();

            NormalizedRGBA warna = sensorWarna.getNormalizedColors();
            float[] hsv = new float[3];
            Color.RGBToHSV((int) (warna.red * 255), (int) (warna.green * 255), (int) (warna.blue * 255), hsv);

            telemetry.addData("METODE", "3 — Deteksi Hue");
            telemetry.addData("Hue", "%.1f derajat", hsv[0]);
            telemetry.addData("Saturasi", "%.2f", hsv[1]);
            telemetry.addData("Value (kecerahan)", "%.2f", hsv[2]);
            telemetry.addData("Hasil", hasil);
            telemetry.update();
        }
    }

    /**
     * Fungsi klasifikasi warna yang dipakai ulang di metode 3 dan
     * metode 4 — pola yang sama seperti belokPID() dipakai ulang
     * lintas file di Auto05/Auto08.
     */
    private String bacaWarna() {

        NormalizedRGBA warna = sensorWarna.getNormalizedColors();
        float[] hsv = new float[3];
        Color.RGBToHSV((int) (warna.red * 255), (int) (warna.green * 255), (int) (warna.blue * 255), hsv);
        float hue = hsv[0];
        float saturasi = hsv[1];

        if (saturasi < SATURASI_MINIMUM) return "TIDAK ADA WARNA JELAS";

        if (hue <= HUE_MERAH_MAKS || hue >= HUE_MERAH_MIN) return "MERAH";
        if (hue >= HUE_BIRU_MIN && hue <= HUE_BIRU_MAKS) return "BIRU";
        return "TIDAK DIKETAHUI";
    }

    // ========================================================================
    //   METODE 4 — DIPAKAI BUAT KEPUTUSAN OTONOM
    // ========================================================================
    /**
     * INI KENAPA SENSOR WARNA BERGUNA DI KOMPETISI:
     *   Baca warna SEKALI, lalu CABANGKAN jalur autonomous berdasarkan
     *   hasilnya. Contoh: kalau elemen permainan/tanda aliansi warna
     *   MERAH, robot ambil rute A; kalau BIRU, rute B.
     *
     *   Ini pola "sense lalu putuskan" — baca sensor SEKALI di awal
     *   (bukan terus-menerus kayak metode 1-3), lalu jalankan salah
     *   satu dari beberapa rute yang SUDAH disiapkan sebelumnya.
     *
     *   Rute di sini sengaja dibikin sederhana (belok kiri/kanan
     *   berbasis waktu, kayak Auto09) supaya fokus pelajaran tetap di
     *   POLA PERCABANGANNYA. Robot kompetisi sungguhan biasanya
     *   manggil pergiKeTitik() dari Auto08 di sini, bukan belok
     *   sederhana ini.
     */
    private void metode4_KeputusanOtonom() {

        String warnaTerdeteksi = bacaWarna();

        telemetry.addData("METODE", "4 — Keputusan Otonom");
        telemetry.addData("Warna terdeteksi", warnaTerdeteksi);
        telemetry.update();
        sleep(1000); // jeda sebentar biar kebaca jelas di Driver Station

        if (warnaTerdeteksi.equals("MERAH")) {
            telemetry.addLine("-> Mengambil RUTE A (belok kiri)");
            telemetry.update();
            putarSelama(-KECEPATAN, KECEPATAN, WAKTU_GERAK_DETIK);
        } else if (warnaTerdeteksi.equals("BIRU")) {
            telemetry.addLine("-> Mengambil RUTE B (belok kanan)");
            telemetry.update();
            putarSelama(KECEPATAN, -KECEPATAN, WAKTU_GERAK_DETIK);
        } else {
            telemetry.addLine("-> Warna nggak jelas, robot diam (rute default aman)");
            telemetry.update();
        }
    }

    private void putarSelama(double powerKiri, double powerKanan, double detik) {
        ElapsedTime waktu = new ElapsedTime();
        while (opModeIsActive() && waktu.seconds() < detik) {
            motorKiri.setPower(powerKiri);
            motorKanan.setPower(powerKanan);
        }
        motorKiri.setPower(0);
        motorKanan.setPower(0);
    }
}

/*
 * ============================================================================
 *   LATIHAN — kerjakan berurutan
 * ============================================================================
 *
 *  PERCOBAAN 1 — Rasakan masalah jarak/cahaya
 *    a. Jalankan metode 1, dekatkan benda merah, catat RGB-nya
 *    b. Jauhkan pelan-pelan (masih dalam jangkauan), catat lagi.
 *       Semua angka ikut turun bersama?
 *
 *  PERCOBAAN 2 — Buktikan metode naif rapuh
 *    a. Jalankan metode 2 dengan benda merah DEKAT sensor (harusnya
 *       "MERAH")
 *    b. Jauhkan pelan-pelan sampai berubah jadi "TIDAK DIKETAHUI".
 *       Ukur di jarak berapa itu terjadi
 *
 *  PERCOBAAN 3 — Buktikan hue lebih stabil
 *    a. Ulangi Percobaan 2 tapi pakai metode 3
 *    b. Di jarak yang sama seperti Percobaan 2, apa metode 3 masih
 *       kebaca "MERAH" dengan benar?
 *
 *  PERCOBAAN 4 — Uji batas saturasi
 *    a. Jalankan metode 3, dekatkan kertas PUTIH atau benda ABU-ABU
 *    b. Perhatikan "Saturasi" di telemetry — rendah?
 *    c. Perhatikan "Hasil" — apa dia dengan benar bilang "TIDAK ADA
 *       WARNA JELAS", bukan salah tebak MERAH/BIRU?
 *
 *  TANTANGAN
 *    a. Ganti rute sederhana di metode 4 dengan pemanggilan
 *       pergiKeTitik() dari Auto08_Navigasi (butuh salin hardware +
 *       fungsi odometrinya juga ke file ini, atau jadikan latihan
 *       menggabungkan file). Robot kompetisi sungguhan biasanya
 *       persis begini: satu pembacaan sensor di awal match, menentukan
 *       SELURUH rute autonomous yang dijalankan setelahnya.
 * ============================================================================
 */
