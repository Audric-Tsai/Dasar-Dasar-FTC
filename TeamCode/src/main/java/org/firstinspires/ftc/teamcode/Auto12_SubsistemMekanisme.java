package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * ============================================================================
 *   AUTO SUBSISTEM MEKANISME — Semua Pelajaran, Satu Robot
 * ============================================================================
 *
 *   Ini penutup ARC PERTAMA seri Auto0N — "apa saja yang bisa
 *   dilakukan robot ini secara fisik" (Auto01-11). File ini nggak
 *   punya konsep baru. Ini "ambil-dan-taruh" (pick and place)
 *   sungguhan, dibangun dari potongan-potongan yang SUDAH kamu
 *   pelajari:
 *
 *     Auto01 -> gerak drivetrain dasar, dan kenapa "nebak pakai waktu"
 *               nggak bisa diandalkan (metode 1 di bawah nunjukkin
 *               INI LAGI, kali ini buat SELURUH tugas, bukan cuma
 *               satu gerakan)
 *     Auto06 -> servo buat capit (LANGKAH_PER_LOOP, gerak halus)
 *     Auto09 -> mesin keadaan, servo+motor konkuren, non-blocking
 *     Auto10 -> sensor jarak, berhenti berdasar APA YANG DILIHAT
 *     Auto11 -> sensor warna, verifikasi SEBELUM lanjut, bukan asal
 *               percaya "sudah pasti kepegang"
 *
 *   Lihat juga mechanisms/launcher/ dan mechanisms/intake/ buat
 *   gambaran mekanisme fisik nyata yang bisa dipasangi pola kode yang
 *   sama seperti file ini.
 *
 *   ALUR TUGASNYA:
 *     1. Maju menuju objek, berhenti berdasar SENSOR JARAK (bukan
 *        jarak tebakan) — Auto10
 *     2. Tutup capit SAMBIL baca sensor warna — konkuren, Auto09
 *     3. Kalau warnanya BENAR: bawa ke titik taruh, lepas -> BERHASIL
 *        Kalau warnanya SALAH: langsung lepas di tempat -> GAGAL,
 *        jangan buang waktu bawa objek yang salah ke titik taruh
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive -> drivetrain
 *     servo_lengan             -> capit (servo posisi)
 *     sensor_jarak             -> deteksi objek di depan
 *     sensor_warna             -> verifikasi warna objek yang dipegang
 *
 *   LANJUT KE: Auto13_KinematikaMecanum — ARC KEDUA seri ini dimulai
 *   di sana. Auto13-16 nggak nambah kemampuan robot yang baru;
 *   mereka membenahi CARA kamu menulis, tuning, dan mengatur kode
 *   robot yang sudah bisa "melakukan segalanya" seperti file ini.
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Subsistem Mekanisme (Belajar)", group = "Belajar")
public class Auto12_SubsistemMekanisme extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    private static final double KECEPATAN = 0.3;
    private static final double JARAK_AMBIL_INCI = 4.0;

    private static final double POSISI_TUTUP = 0.0;
    private static final double POSISI_BUKA  = 1.0;
    private static final double LANGKAH_PER_LOOP = 0.01;
    private static final int    JEDA_LANGKAH_MS  = 15;

    /** Warna objek yang BENAR buat diambil. Ganti sesuai aliansi/strategi. */
    private static final String WARNA_TARGET = "MERAH";

    private static final float HUE_MERAH_MAKS = 30f;
    private static final float HUE_MERAH_MIN  = 330f;
    private static final float HUE_BIRU_MIN   = 180f;
    private static final float HUE_BIRU_MAKS  = 270f;
    private static final float SATURASI_MINIMUM = 0.4f;

    /**
     * Gerak ke titik taruh & balik ditulis pakai WAKTU sederhana di
     * sini (bukan Auto08 pergiKeTitik) supaya file ini fokus ke pola
     * INTEGRASI SUBSISTEM-nya. Robot kompetisi sungguhan ganti dua
     * baris waktu ini dengan panggilan pergiKeTitik() yang sudah
     * dibahas lengkap di Auto08_Navigasi — lihat TANTANGAN di bawah.
     */
    private static final double WAKTU_KE_TARGET_DETIK = 1.5;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor motorKiri;
    private DcMotor motorKanan;
    private Servo   servoLengan;
    private DistanceSensor sensorJarak;
    private NormalizedColorSensor sensorWarna;

    // ========================================================================
    //   BAGIAN 3 — PROGRAM UTAMA
    // ========================================================================

    @Override
    public void runOpMode() {

        motorKiri  = hardwareMap.get(DcMotor.class, "left_drive");
        motorKanan = hardwareMap.get(DcMotor.class, "right_drive");
        motorKiri.setDirection(DcMotor.Direction.REVERSE);
        motorKanan.setDirection(DcMotor.Direction.FORWARD);
        motorKiri.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorKanan.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        servoLengan = hardwareMap.get(Servo.class, "servo_lengan");
        servoLengan.setPosition(POSISI_BUKA); // mulai TERBUKA, siap ambil

        sensorJarak = hardwareMap.get(DistanceSensor.class, "sensor_jarak");
        sensorWarna = hardwareMap.get(NormalizedColorSensor.class, "sensor_warna");

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.addLine("Taruh objek MERAH/BIRU di depan robot.");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_Sederhana();
        //metode2_MesinKeadaanLengkap();
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(3000);
    }

    // ========================================================================
    //   METODE 1 — CARA SEDERHANA (KEMBALI KE POLA AUTO01 METODE 1)
    // ========================================================================
    /**
     * IDENYA:
     *   Nebak semuanya pakai WAKTU. Maju sekian detik (semoga sampai
     *   ke objek), tutup capit (semoga objeknya beneran ada di
     *   antara jepitan), maju lagi sekian detik ke titik taruh, buka.
     *
     * KENAPA INI DITULIS ULANG DI SINI:
     *   Supaya kelihatan JELAS betapa jauh bedanya dibanding metode 2.
     *   Ini punya SEMUA kelemahan yang sudah kamu pelajari sepanjang
     *   seri ini, sekaligus:
     *
     *     - Kayak Auto01 metode 1: "maju sekian detik" meleset kalau
     *       posisi awal objek beda dari yang diperkirakan, atau
     *       baterai nggak penuh.
     *     - Kayak Auto06: setPosition() capit itu FIRE-AND-FORGET —
     *       kode ini nggak pernah benar-benar tahu apa objeknya
     *       ketangkep atau nggak.
     *     - TIDAK ADA verifikasi warna sama sekali — kalau objek yang
     *       ketangkep ternyata warna SALAH, robot tetap bawa dia ke
     *       titik taruh dan nganggap itu SUKSES.
     *
     *   Ini bukan cuma "kurang bagus" — di kompetisi sungguhan, ini
     *   cara paling umum autonomous KEHILANGAN POIN tanpa robotnya
     *   sendiri "sadar" ada yang salah.
     */
    private void metode1_Sederhana() {

        telemetry.addData("METODE", "1 — Sederhana (tanpa sensor)");
        telemetry.update();

        // Maju "semoga sampai" ke objek.
        motorKiri.setPower(KECEPATAN);
        motorKanan.setPower(KECEPATAN);
        sleep((long) (WAKTU_KE_TARGET_DETIK * 1000));
        berhenti();

        // Tutup capit, "semoga" ada objek di antaranya.
        servoLengan.setPosition(POSISI_TUTUP);
        sleep(1000);

        // Maju lagi ke titik taruh, nggak peduli warnanya benar atau nggak.
        motorKiri.setPower(KECEPATAN);
        motorKanan.setPower(KECEPATAN);
        sleep((long) (WAKTU_KE_TARGET_DETIK * 1000));
        berhenti();

        servoLengan.setPosition(POSISI_BUKA);
        sleep(1000);

        telemetry.addLine("Selesai — tapi robot nggak pernah TAHU apa ini beneran berhasil.");
        telemetry.update();
    }

    // ========================================================================
    //   METODE 2 — MESIN KEADAAN LENGKAP (SEMUA SUBSISTEM TERPADU)
    // ========================================================================
    /**
     * TAHAP:
     *
     *   MENUJU_OBJEK   -> maju, berhenti berdasar SENSOR JARAK (Auto10),
     *                     bukan waktu/encoder tebakan.
     *
     *   AMBIL_DAN_PERIKSA -> tutup capit (gerak halus, non-blocking,
     *                     Auto09) SAMBIL baca sensor warna berulang.
     *                     Begitu capit selesai menutup, HASIL BACAAN
     *                     WARNA TERAKHIR dipakai buat memutuskan
     *                     tahap berikutnya — verifikasi SEBELUM
     *                     melangkah, bukan asumsi "pasti berhasil".
     *
     *   MENUJU_TARGET  -> (cuma kalau warnanya BENAR) bawa ke titik
     *                     taruh.
     *
     *   LEPAS_BERHASIL -> buka capit di titik taruh.
     *   LEPAS_GAGAL    -> buka capit DI TEMPAT (nggak buang waktu
     *                     bawa objek yang salah ke mana pun).
     *
     *   SELESAI        -> berhenti, laporkan hasil akhir.
     *
     * BANDINGKAN DENGAN METODE 1:
     *   Coba taruh objek warna SALAH di depan robot buat kedua
     *   metode. Metode 1 tetap "menganggap sukses". Metode 2
     *   melaporkan GAGAL dengan jujur DAN nggak buang-buang waktu
     *   bawa objek yang salah ke titik taruh — dia langsung
     *   melepasnya di tempat supaya autonomous bisa lanjut ke
     *   strategi lain kalau ada waktu tersisa.
     */
    private enum Tahap { MENUJU_OBJEK, AMBIL_DAN_PERIKSA, MENUJU_TARGET, LEPAS_BERHASIL, LEPAS_GAGAL, SELESAI }

    private void metode2_MesinKeadaanLengkap() {

        Tahap tahap = Tahap.MENUJU_OBJEK;
        String warnaTerdeteksi = "BELUM DIBACA";

        double posisiServoSekarang = servoLengan.getPosition();
        boolean servoSelesai = false;

        ElapsedTime waktuTahap = new ElapsedTime();
        ElapsedTime waktuServoLoop = new ElapsedTime();

        while (opModeIsActive() && tahap != Tahap.SELESAI) {

            switch (tahap) {

                case MENUJU_OBJEK:
                    if (sensorJarak.getDistance(DistanceUnit.INCH) > JARAK_AMBIL_INCI) {
                        motorKiri.setPower(KECEPATAN);
                        motorKanan.setPower(KECEPATAN);
                    } else {
                        berhenti();
                        waktuTahap.reset();
                        tahap = Tahap.AMBIL_DAN_PERIKSA;
                    }
                    break;

                case AMBIL_DAN_PERIKSA:
                    // Sub-tugas A: tutup capit, non-blocking (pola Auto09).
                    if (!servoSelesai && waktuServoLoop.milliseconds() >= JEDA_LANGKAH_MS) {
                        waktuServoLoop.reset();
                        if (Math.abs(POSISI_TUTUP - posisiServoSekarang) > LANGKAH_PER_LOOP) {
                            posisiServoSekarang -= LANGKAH_PER_LOOP;
                            servoLengan.setPosition(posisiServoSekarang);
                        } else {
                            servoLengan.setPosition(POSISI_TUTUP);
                            servoSelesai = true;
                        }
                    }
                    // Sub-tugas B: baca warna TIAP LOOP (konkuren dengan A).
                    warnaTerdeteksi = bacaWarna();

                    if (servoSelesai) {
                        waktuTahap.reset();
                        if (warnaTerdeteksi.equals(WARNA_TARGET)) {
                            tahap = Tahap.MENUJU_TARGET;
                        } else {
                            tahap = Tahap.LEPAS_GAGAL;
                        }
                    }
                    break;

                case MENUJU_TARGET:
                    if (waktuTahap.seconds() < WAKTU_KE_TARGET_DETIK) {
                        motorKiri.setPower(KECEPATAN);
                        motorKanan.setPower(KECEPATAN);
                    } else {
                        berhenti();
                        posisiServoSekarang = servoLengan.getPosition();
                        servoSelesai = false;
                        tahap = Tahap.LEPAS_BERHASIL;
                    }
                    break;

                case LEPAS_BERHASIL:
                case LEPAS_GAGAL:
                    if (!servoSelesai && waktuServoLoop.milliseconds() >= JEDA_LANGKAH_MS) {
                        waktuServoLoop.reset();
                        if (Math.abs(POSISI_BUKA - posisiServoSekarang) > LANGKAH_PER_LOOP) {
                            posisiServoSekarang += LANGKAH_PER_LOOP;
                            servoLengan.setPosition(posisiServoSekarang);
                        } else {
                            servoLengan.setPosition(POSISI_BUKA);
                            servoSelesai = true;
                        }
                    }
                    if (servoSelesai) tahap = Tahap.SELESAI;
                    break;

                case SELESAI:
                    break;
            }

            telemetry.addData("METODE", "2 — Mesin Keadaan Lengkap");
            telemetry.addData("TAHAP", tahap);
            telemetry.addData("Warna terdeteksi", warnaTerdeteksi);
            telemetry.update();
        }

        berhenti();

        telemetry.addData("HASIL AKHIR", tahap == Tahap.SELESAI
                ? (warnaTerdeteksi.equals(WARNA_TARGET) ? "BERHASIL" : "GAGAL — warna salah, dilepas di tempat")
                : "?");
        telemetry.update();
    }

    // ========================================================================
    //   FUNGSI PEMBANTU — sama seperti Auto11
    // ========================================================================

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

    private void berhenti() {
        motorKiri.setPower(0);
        motorKanan.setPower(0);
    }
}

/*
 * ============================================================================
 *   LATIHAN — kerjakan berurutan
 * ============================================================================
 *
 *  PERCOBAAN 1 — Bandingkan langsung
 *    a. Taruh objek warna BENAR (sesuai WARNA_TARGET) di jarak yang
 *       agak beda dari biasanya
 *    b. Jalankan metode 1, lihat apa capitnya ketutup pas ada objeknya
 *    c. Jalankan metode 2, bandingkan — apa dia berhenti di jarak
 *       yang lebih konsisten?
 *
 *  PERCOBAAN 2 — Uji objek warna salah
 *    a. Taruh objek warna SALAH (bukan WARNA_TARGET) di depan robot
 *    b. Jalankan metode 1 — robot "menganggap" sukses?
 *    c. Jalankan metode 2 — lihat "HASIL AKHIR" di telemetry. Apa dia
 *       jujur bilang GAGAL, dan apa dia beneran nggak buang waktu
 *       bawa objeknya ke titik taruh?
 *
 *  PERCOBAAN 3 — Ukur waktu total
 *    a. Ukur berapa detik metode 1 total berjalan
 *    b. Ukur metode 2. Lebih cepat? (Ingat pelajaran Auto09 soal
 *       tugas konkuren)
 *
 *  TANTANGAN — proyek penutup arc pertama
 *    a. Ganti MENUJU_OBJEK dan MENUJU_TARGET di metode 2 supaya
 *       pakai pergiKeTitik() dari Auto08_Navigasi (butuh bawa
 *       hardware odometri + fungsi perbaruiPosisi() ke file ini juga)
 *       alih-alih sensor jarak/waktu sederhana
 *    b. Tambah tahap LEPAS_GAGAL supaya robot MUNDUR sedikit dan
 *       COBA LAGI (kembali ke MENUJU_OBJEK) alih-alih langsung
 *       menyerah — batasi percobaan ulang maksimal 2x biar nggak
 *       nyangkut selamanya kalau memang nggak ada objek yang benar
 *    c. Ini proyek penutup arc pertama yang realistis: robot yang menavigasi pakai
 *       koordinat (Auto08), mengambil objek berdasar sensor (Auto10/
 *       11), memverifikasi sebelum bertindak (Auto11), dan mengatur
 *       semuanya lewat mesin keadaan non-blocking (Auto09) — persis
 *       arsitektur yang dipakai tim FTC kompetitif sungguhan.
 * ============================================================================
 */
