package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * ============================================================================
 *   AUTO SERVO — Mengenal Servo Lewat REV Smart Robot Servo
 * ============================================================================
 *
 *   Beda besar dari Auto01-04: file-file itu semua soal motorKiri dan
 *   motorKanan (DcMotor + encoder). File ini soal jenis aktuator yang
 *   SAMA SEKALI BEDA CARA KERJANYA — servo.
 *
 *   "REV" DI JUDUL INI CUMA SOAL MEREK HARDWARE:
 *   REV Smart Robot Servo (casing biru) itu servo yang lebih kuat dan
 *   lebih cepat dari servo hobi biasa, tapi dia tetap dikendalikan
 *   lewat kelas Java yang SAMA PERSIS: Servo dan CRServo. Beda merek
 *   servo cuma beda di spek (torsi, kecepatan, rentang derajat) —
 *   bukan beda di cara nulis kodenya. Ini beda dari Auto01, di mana
 *   ganti tipe motor REV HARUS mengubah TICK_PER_PUTARAN di kode.
 *
 *   PERBEDAAN PALING PENTING — SERVO ITU OPEN LOOP:
 *   Ingat di Auto01, motorKiri.getCurrentPosition() bisa kasih tahu
 *   "aku sudah sampai tick sekian". DcMotor + encoder itu CLOSED
 *   LOOP — dia melapor balik.
 *
 *   Servo TIDAK BEGITU. servoLengan.setPosition(1.0) itu perintah
 *   SEKALI KIRIM, LUPAKAN (fire-and-forget). Kode kamu nggak pernah
 *   tahu apakah servo-nya beneran sampai, ketahan macet oleh
 *   sesuatu, atau lepas dari dudukannya. Ini sebabnya servo cocok
 *   buat mekanisme RINGAN yang jarang macet (capit, klep, indexer),
 *   bukan buat beban berat yang butuh kepastian posisi.
 *
 *   ROBOT INI PAKAI:
 *     servo_lengan  -> Servo biasa (posisi 0.0 - 1.0), contoh: capit
 *     servo_roller  -> CRServo (continuous rotation), contoh: roller
 *
 *   File ini BUKAN bagian dari drivetrain — sengaja berdiri sendiri
 *   supaya bisa dites di meja tanpa nyalain motor kiri/kanan dulu.
 *
 *   LANJUT KE: Auto07_Odometri — balik lagi ke drivetrain, tapi
 *   dengan pertanyaan baru: bukan "gerak ke mana", tapi "robotnya
 *   tahu nggak dia sekarang ada di mana".
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Servo (Belajar)", group = "Belajar")
public class Auto06_Servo extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    /**
     * POSISI TERTUTUP / TERBUKA
     *
     * Servo dikendalikan pakai angka 0.0 - 1.0, bukan derajat
     * langsung. Angka ini dipetakan ke rentang gerak fisik servo
     * (buat REV Smart Robot Servo, defaultnya kira-kira 0-200 derajat,
     * tapi UKUR SENDIRI robotmu, jangan percaya angka di internet).
     */
    private static final double POSISI_TUTUP = 0.0;
    private static final double POSISI_BUKA  = 1.0;

    /**
     * BATAS AMAN (dipakai metode 3)
     *
     * Kalau mekanisme fisikmu nggak bisa berayun penuh 0.0-1.0 tanpa
     * kejedot bracket/kabel, batasi di sini. UKUR SENDIRI di robotmu.
     */
    private static final double BATAS_AMAN_MIN = 0.2;
    private static final double BATAS_AMAN_MAKS = 0.8;

    /**
     * LANGKAH & JEDA (dipakai metode 2)
     *
     * Servo sebenarnya sudah punya kecepatan internal sendiri yang
     * cukup cepat. Dua angka ini yang mengatur seberapa PELAN kita
     * MEMAKSA dia bergerak, dengan cara mengirim posisi-posisi
     * kecil berturut-turut alih-alih satu lompatan besar.
     *
     * LANGKAH_PER_LOOP kecil + JEDA_LOOP_MS besar = gerakan pelan.
     */
    private static final double LANGKAH_PER_LOOP = 0.01;
    private static final int    JEDA_LOOP_MS = 15;

    /** Kecepatan CRServo, -1.0 (mundur penuh) sampai 1.0 (maju penuh). */
    private static final double CR_KECEPATAN = 0.6;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private Servo   servoLengan;
    private CRServo servoRoller;

    // ========================================================================
    //   BAGIAN 3 — PROGRAM UTAMA
    // ========================================================================

    @Override
    public void runOpMode() {

        servoLengan = hardwareMap.get(Servo.class, "servo_lengan");
        servoRoller = hardwareMap.get(CRServo.class, "servo_roller");

        // Servo TIDAK PUNYA "posisi awal" bawaan yang bisa dipercaya —
        // dia cuma mengingat perintah TERAKHIR yang pernah dikirim.
        // Kalau OpMode sebelumnya berhenti di tengah gerakan, atau
        // robot baru dinyalakan, posisi fisiknya bisa di mana saja.
        // Selalu set posisi awal yang JELAS sebelum waitForStart().
        servoLengan.setPosition(POSISI_TUTUP);

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_PosisiInstan();
        //metode2_PosisiHalus();
        //metode3_BatasiRange();
        //metode4_CRServoContinuous();
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(2000);
    }

    // ========================================================================
    //   METODE 1 — LANGSUNG LOMPAT (INSTAN)
    // ========================================================================
    /**
     * IDENYA:
     *   setPosition() langsung dikirim, servo bergerak SECEPAT
     *   MUNGKIN yang dia bisa ke posisi itu.
     *
     * KENAPA INI KADANG MASALAH:
     *   Servo yang kuat (kayak REV Smart Robot Servo) bergerak
     *   CEPAT. Kalau di ujungnya ada beban dengan lengan panjang
     *   (misalnya capit di ujung lengan), lompatan instan bisa bikin
     *   mekanismenya HAJAR balik ke bodi robot, nyenggol barang di
     *   lapangan, atau lama-lama bikin gear/mounting-nya kendor
     *   karena kena hentakan terus-menerus.
     *
     * INGAT SIFAT OPEN-LOOP DI ATAS:
     *   Coba tahan lengan servo pakai tangan sebentar waktu metode
     *   ini jalan (JANGAN LAMA-LAMA — servo bisa kepanasan kalau
     *   ditahan/stall terlalu lama). Kode-nya nggak akan pernah tahu
     *   kamu sedang menahannya. Nggak ada error, nggak ada
     *   peringatan. Itulah open-loop.
     */
    private void metode1_PosisiInstan() {

        telemetry.addData("METODE", "1 — Posisi Instan");
        telemetry.addLine("Bergerak ke TERBUKA...");
        telemetry.update();
        servoLengan.setPosition(POSISI_BUKA);
        sleep(1500);

        telemetry.addLine("Bergerak ke TERTUTUP...");
        telemetry.update();
        servoLengan.setPosition(POSISI_TUTUP);
        sleep(1500);
    }

    // ========================================================================
    //   METODE 2 — GERAK HALUS (SOFTWARE RAMP)
    // ========================================================================
    /**
     * IDENYA:
     *   Servo nggak punya fungsi bawaan "gerak pelan-pelan". Jadi
     *   kita TIPU dia: kirim banyak perintah posisi KECIL secara
     *   berurutan, bukan satu perintah besar. Dari luar, gerakannya
     *   kelihatan halus dan terkendali kecepatannya.
     *
     * BEDA DENGAN Auto01 metode 4:
     *   Di sana, perlambatan terjadi karena MOTOR ENCODER melapor
     *   posisi asli secara terus-menerus (closed loop). Di sini,
     *   kita nggak punya laporan posisi asli sama sekali — jadi kita
     *   cuma bisa MENGASUMSIKAN servo mengikuti tiap perintah kecil
     *   yang dikirim. Kalau servo-nya ketahan sesuatu di tengah
     *   jalan, "posisi sekarang" versi kode ini akan salah, dan
     *   kode nggak akan pernah tahu.
     */
    private void metode2_PosisiHalus() {

        telemetry.addData("METODE", "2 — Gerak Halus");
        telemetry.update();

        gerakkanHalus(POSISI_BUKA);
        sleep(500);
        gerakkanHalus(POSISI_TUTUP);
    }

    private void gerakkanHalus(double posisiTarget) {

        double posisiSekarang = servoLengan.getPosition();
        double arah = Math.signum(posisiTarget - posisiSekarang);

        while (opModeIsActive() && Math.abs(posisiTarget - posisiSekarang) > LANGKAH_PER_LOOP) {

            posisiSekarang += arah * LANGKAH_PER_LOOP;
            servoLengan.setPosition(posisiSekarang);

            telemetry.addData("Menuju", "%.2f", posisiTarget);
            telemetry.addData("Posisi (perintah terakhir)", "%.3f", posisiSekarang);
            telemetry.update();

            sleep(JEDA_LOOP_MS);
        }

        servoLengan.setPosition(posisiTarget); // pastikan pas di ujung, bukan hampir
    }

    // ========================================================================
    //   METODE 3 — BATASI RENTANG GERAK (scaleRange)
    // ========================================================================
    /**
     * IDENYA:
     *   scaleRange(min, max) memetakan ULANG seluruh rentang 0.0-1.0
     *   supaya cuma dipakai di antara min dan max yang kamu tentukan.
     *
     *   Setelah dipanggil:
     *     setPosition(0.0)  -> sekarang beneran berhenti di BATAS_AMAN_MIN
     *     setPosition(1.0)  -> sekarang beneran berhenti di BATAS_AMAN_MAKS
     *     setPosition(0.5)  -> tepat di tengah-tengah dua batas itu
     *
     * KENAPA INI PENTING BUAT REV SMART ROBOT SERVO:
     *   Servo ini kuat dan rentang geraknya lebar. Kalau mekanismemu
     *   secara fisik cuma boleh berayun sebagian dari rentang penuh
     *   itu (karena kejedot bracket, kabel, atau bodi robot sendiri),
     *   memaksa dia ke 0.0 atau 1.0 mentah bisa bikin dia NGOTOT
     *   dorong ke arah yang sudah mentok — motor internalnya tetap
     *   coba gerak, gearnya stall, dan lama-lama itu yang merusak
     *   servo, bukan pemakaian normalnya.
     *
     *   Dengan scaleRange, SEMUA kode lain yang manggil setPosition
     *   (termasuk metode 1 dan 2 di atas) otomatis ikut aman, tanpa
     *   perlu mengubah angka 0.0/1.0 di tiap tempat yang manggilnya.
     */
    private void metode3_BatasiRange() {

        servoLengan.scaleRange(BATAS_AMAN_MIN, BATAS_AMAN_MAKS);

        telemetry.addData("METODE", "3 — Batasi Rentang");
        telemetry.addData("Rentang aman", "%.2f - %.2f", BATAS_AMAN_MIN, BATAS_AMAN_MAKS);
        telemetry.update();

        servoLengan.setPosition(0.0);
        sleep(1500);

        servoLengan.setPosition(1.0);
        sleep(1500);
    }

    // ========================================================================
    //   METODE 4 — CRSERVO (CONTINUOUS ROTATION)
    // ========================================================================
    /**
     * BEDA TOTAL DARI METODE 1-3:
     *   CRServo BUKAN servo posisi. Dia nggak punya konsep "sudut
     *   target" sama sekali — API-nya cuma setPower(), sama seperti
     *   DcMotor. Bedanya dengan DcMotor: nggak ada encoder, jadi
     *   nggak ada getCurrentPosition(), nggak ada RUN_TO_POSITION,
     *   nggak ada apa-apa soal posisi.
     *
     * KAPAN PAKAI CRSERVO DARIPADA DCMOTOR:
     *   Buat mekanisme yang cuma perlu MUTER TERUS (roller intake,
     *   spinner), di mana kamu nggak pernah butuh tahu "sudah muter
     *   berapa kali". CRServo lebih ringan, lebih murah, dan colok
     *   ke port servo — nggak makan jatah port motor yang terbatas
     *   di Control/Expansion Hub.
     *
     *   Kalau kamu BUTUH tahu posisi/kecepatan asli (misalnya buat
     *   PID kayak Auto04), itu pertanda kamu butuh DcMotor + encoder,
     *   bukan CRServo.
     */
    private void metode4_CRServoContinuous() {

        telemetry.addData("METODE", "4 — CRServo");
        telemetry.addLine("Muter maju...");
        telemetry.update();
        servoRoller.setPower(CR_KECEPATAN);
        sleep(2000);

        servoRoller.setPower(0);
        sleep(500);

        telemetry.addLine("Muter mundur...");
        telemetry.update();
        servoRoller.setPower(-CR_KECEPATAN);
        sleep(2000);

        servoRoller.setPower(0);
    }
}

/*
 * ============================================================================
 *   LATIHAN — kerjakan berurutan
 * ============================================================================
 *
 *  PERCOBAAN 1 — Ukur rentang derajat aslinya
 *    a. Jalankan metode 1
 *    b. Pakai busur derajat, ukur sudut fisik di POSISI_TUTUP dan
 *       POSISI_BUKA
 *    c. Berapa total derajatnya? Sama dengan spek yang kamu kira?
 *
 *  PERCOBAAN 2 — Rasakan open-loop
 *    a. Jalankan metode 1
 *    b. Waktu servo lagi bergerak, tahan lengannya SEBENTAR pakai
 *       tangan (jangan lama, servo bisa panas kalau stall lama)
 *    c. Lihat telemetry — ada tanda apa pun kalau servo lagi
 *       ketahan? (Jawabannya: tidak ada)
 *
 *  PERCOBAAN 3 — Bandingkan kecepatan
 *    a. Jalankan metode 1, amati kecepatan gerak
 *    b. Jalankan metode 2, amati lagi
 *    c. Ganti JEDA_LOOP_MS jadi 50, jalankan metode 2 lagi. Lebih
 *       pelan?
 *
 *  PERCOBAAN 4 — Coba scaleRange
 *    a. Jalankan metode 3 dengan BATAS_AMAN_MIN/MAKS default
 *    b. Ganti jadi 0.4 - 0.6 (rentang sempit banget)
 *    c. Jalankan lagi. Gerakannya jauh lebih pendek?
 *
 *  TANTANGAN
 *    a. Tulis metode5_CapitOtomatis() yang menggabungkan metode 2
 *       (gerak halus) dan metode 3 (rentang aman) sekaligus — servo
 *       bergerak PELAN, TAPI juga nggak pernah keluar dari rentang
 *       yang aman. Ini pola yang biasa dipakai buat capit/klep
 *       sungguhan di robot kompetisi.
 * ============================================================================
 */
