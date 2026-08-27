package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * ============================================================================
 *   AUTO KINEMATIKA MECANUM — Gerak yang MUSTAHIL Buat Drivetrain Auto01-12
 * ============================================================================
 *
 *   SEMUA file sebelum ini (Auto01-12) diam-diam mengasumsikan
 *   drivetrain TANK/DIFFERENTIAL: 2 motor, kiri dan kanan, dan robot
 *   cuma bisa jalan maju/mundur atau muter — nggak bisa geser
 *   samping tanpa muter dulu.
 *
 *   Kalau robotmu pakai RODA MECANUM (4 motor, roller di tiap roda
 *   miring 45 derajat), robot bisa GESER SAMPING secara langsung,
 *   bahkan JALAN DIAGONAL, tanpa perlu muter badan sama sekali. Ini
 *   namanya HOLONOMIC — jumlah "arah gerak bebas" robot (maju/mundur,
 *   geser, muter) sama dengan jumlah motor yang mengendalikannya
 *   secara independen.
 *
 *   KALAU ROBOTMU BUKAN MECANUM: lewati file ini, nggak berlaku buat
 *   drivetrain tank/differential. Auto14-16 nggak bergantung ke file
 *   ini, aman dilewati.
 *
 *   CARA KERJA FISIKNYA (singkat):
 *   Tiap roda mecanum punya roller-roller kecil di sekitar
 *   lingkarannya, dipasang miring 45 derajat. Waktu roda berputar,
 *   gaya dorongannya nggak PERSIS ke arah depan-belakang — sebagian
 *   "dibelokkan" ke samping oleh roller yang miring itu. Dengan
 *   mengatur 4 motor berputar dengan KOMBINASI arah dan kecepatan
 *   yang tepat, gaya-gaya serong dari keempat roda itu bisa saling
 *   MENAMBAH ke satu arah (jadi geser murni) atau saling MENIADAKAN
 *   sebagiannya (jadi kombinasi maju+geser = diagonal).
 *
 *   RUMUSNYA (SAMA PERSIS dengan sample resmi FTC SDK —
 *   lihat FtcRobotController/.../samples/BasicOmniOpMode_Linear.java,
 *   versi TeleOp dari pola yang sama):
 *
 *     depanKiriPower    = axial + lateral + putar
 *     depanKananPower   = axial - lateral - putar
 *     belakangKiriPower = axial - lateral + putar
 *     belakangKananPower= axial + lateral - putar
 *
 *     axial   = kecepatan maju/mundur   (+ = maju)
 *     lateral = kecepatan geser         (+ = geser ke KANAN)
 *     putar   = kecepatan muter         (+ = muter ke KIRI, sama
 *                                          konvensi IMU di Auto02)
 *
 *   Nilai-nilai ini lalu di-NORMALISASI (dibagi angka terbesarnya,
 *   HANYA kalau ada yang lebih dari 1.0) supaya nggak ada motor yang
 *   dimintain power di luar rentang -1.0 sampai 1.0.
 *
 *   ROBOT INI PAKAI:
 *     depan_kiri, depan_kanan, belakang_kiri, belakang_kanan
 *     -> 4 motor mecanum
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Kinematika Mecanum (Belajar)", group = "Belajar")
public class Auto13_KinematikaMecanum extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    private static final double TICK_PER_PUTARAN   = 560.0;
    private static final double DIAMETER_RODA_INCI = 3.54;
    private static final double KECEPATAN = 0.3;

    /**
     * PENGALI GESER (STRAFE_MULTIPLIER)
     *
     * Roda mecanum geser dengan cara "menyerongkan" roller-nya ke
     * lantai — ini SECARA FISIK lebih boros gesekan/slip
     * dibandingkan menggelinding lurus ke depan (di mana roda
     * menggelinding bersih tanpa perlu roller-nya ikut menyerong).
     * Efeknya: buat menempuh JARAK FISIK yang SAMA, geser samping
     * butuh LEBIH BANYAK putaran roda (lebih banyak tick) daripada
     * jalan lurus.
     *
     * Angka ini biasanya sekitar 1.4-1.6 buat roda mecanum umum,
     * TAPI UKUR SENDIRI robotmu — lihat PERCOBAAN di LATIHAN.
     */
    private static final double STRAFE_MULTIPLIER = 1.5;

    private static final double TOLERANSI_INCI = 0.5;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor motorDepanKiri;
    private DcMotor motorDepanKanan;
    private DcMotor motorBelakangKiri;
    private DcMotor motorBelakangKanan;

    // ========================================================================
    //   BAGIAN 3 — PROGRAM UTAMA
    // ========================================================================

    @Override
    public void runOpMode() {

        motorDepanKiri     = hardwareMap.get(DcMotor.class, "depan_kiri");
        motorDepanKanan    = hardwareMap.get(DcMotor.class, "depan_kanan");
        motorBelakangKiri  = hardwareMap.get(DcMotor.class, "belakang_kiri");
        motorBelakangKanan = hardwareMap.get(DcMotor.class, "belakang_kanan");

        // Sisi kanan dibalik arahnya, sama alasan seperti Auto01:
        // biar "power positif" konsisten berarti "maju" di SEMUA motor.
        motorDepanKiri.setDirection(DcMotor.Direction.REVERSE);
        motorBelakangKiri.setDirection(DcMotor.Direction.REVERSE);
        motorDepanKanan.setDirection(DcMotor.Direction.FORWARD);
        motorBelakangKanan.setDirection(DcMotor.Direction.FORWARD);

        for (DcMotor m : new DcMotor[]{motorDepanKiri, motorDepanKanan, motorBelakangKiri, motorBelakangKanan}) {
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_MajuMundur(1.0);
        //metode2_GeserMurni(1.0);
        //metode3_Diagonal(1.0);
        //metode4_GeserBerEncoder(24);
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(2000);
    }

    // ========================================================================
    //   METODE 1 — MAJU/MUNDUR (SANITY CHECK, BELUM PAKAI KEMAMPUAN KHUSUS)
    // ========================================================================
    /**
     * IDENYA:
     *   axial = KECEPATAN, lateral = 0, putar = 0. Keempat motor
     *   dapat power yang SAMA. Ini nggak beda dari drivetrain tank
     *   Auto01 — belum memakai apa pun yang KHUSUS mecanum.
     *
     * KENAPA MULAI DI SINI:
     *   Sebelum coba geser/diagonal, pastikan dulu KEEMPAT motor
     *   jalan ke arah yang BENAR waktu disuruh maju. Kalau ada satu
     *   motor yang arahnya salah, gerakan maju masih KELIHATAN oke
     *   secara kasar (robot tetap maju kira-kira), tapi metode 2/3
     *   nanti akan langsung kacau nggak jelas kenapa. Cek dari sini
     *   dulu, jangan lompat.
     */
    private void metode1_MajuMundur(double detik) {
        gerakkan(KECEPATAN, 0, 0, detik, "1 — Maju/Mundur");
    }

    // ========================================================================
    //   METODE 2 — GESER MURNI (KEMAMPUAN YANG NGGAK ADA DI TANK DRIVE)
    // ========================================================================
    /**
     * IDENYA:
     *   axial = 0, lateral = KECEPATAN, putar = 0. Robot geser ke
     *   KANAN tanpa maju sedikit pun dan TANPA MUTER SAMA SEKALI.
     *
     * KENAPA INI SIGNIFIKAN:
     *   Coba bayangkan lakuin ini pakai drivetrain tank (Auto01-12).
     *   NGGAK BISA — drivetrain tank cuma bisa mendorong ke arah
     *   depan/belakang rodanya sendiri, mau muter secepat apa pun
     *   nggak akan pernah menghasilkan geseran murni ke samping.
     *   Ini bukan soal "kurang pintar nulis kode" — itu keterbatasan
     *   FISIK bentuk rodanya, dan mecanum secara harfiah dirancang
     *   buat menghilangkan keterbatasan itu.
     *
     * PERHATIKAN WAKTU DIJALANKAN:
     *   Robot mungkin sedikit "goyah" majunya (nggak 100% geser
     *   murni) — itu normal, disebabkan gesekan roller yang nggak
     *   sempurna. Semua 4 motor HARUS berputar (dua ke satu arah,
     *   dua ke arah berlawanan sesuai rumus) — kalau ada motor yang
     *   diam sama sekali, ada yang salah di pengkabelan/konfigurasi.
     */
    private void metode2_GeserMurni(double detik) {
        gerakkan(0, KECEPATAN, 0, detik, "2 — Geser Murni");
    }

    // ========================================================================
    //   METODE 3 — DIAGONAL (AXIAL + LATERAL DIGABUNG)
    // ========================================================================
    /**
     * IDENYA:
     *   axial DAN lateral dua-duanya nggak nol sekaligus. Robot jalan
     *   45 derajat — maju SAMBIL geser — dalam SATU gerakan halus,
     *   bukan dua gerakan terpisah (maju, lalu geser).
     *
     * KENAPA PERLU NORMALISASI:
     *   Coba hitung manual: axial=0.3, lateral=0.3, putar=0.
     *     depanKiriPower = 0.3 + 0.3 + 0 = 0.6   -> masih di dalam batas
     *   Tapi kalau axial=0.7 dan lateral=0.7:
     *     depanKiriPower = 0.7 + 0.7 + 0 = 1.4   -> LEWAT BATAS 1.0!
     *
     *   Motor cuma ngerti power antara -1.0 dan 1.0. Kalau dibiarkan,
     *   nilai 1.4 bakal "dipotong paksa" ke 1.0 oleh SDK, tapi motor
     *   LAIN yang nilainya masih di bawah 1.0 TIDAK ikut terpotong —
     *   akibatnya PERBANDINGAN kekuatan antar motor jadi berubah, dan
     *   robot nggak jalan ke arah 45 derajat yang diminta, melenceng
     *   ke arah lain. Normalisasi (bagi SEMUA motor dengan angka
     *   terbesar) menjaga PERBANDINGANNYA tetap sama, cuma skalanya
     *   yang mengecil.
     */
    private void metode3_Diagonal(double detik) {
        gerakkan(KECEPATAN, KECEPATAN, 0, detik, "3 — Diagonal");
    }

    /** Fungsi inti dipakai metode 1-3: hitung, normalisasi, kirim ke motor. */
    private void gerakkan(double axial, double lateral, double putar, double detik, String namaMetode) {

        double depanKiri     = axial + lateral + putar;
        double depanKanan    = axial - lateral - putar;
        double belakangKiri  = axial - lateral + putar;
        double belakangKanan = axial + lateral - putar;

        double maksimum = Math.max(1.0, Math.max(
                Math.max(Math.abs(depanKiri), Math.abs(depanKanan)),
                Math.max(Math.abs(belakangKiri), Math.abs(belakangKanan))));

        depanKiri     /= maksimum;
        depanKanan    /= maksimum;
        belakangKiri  /= maksimum;
        belakangKanan /= maksimum;

        ElapsedTime waktu = new ElapsedTime();
        while (opModeIsActive() && waktu.seconds() < detik) {
            motorDepanKiri.setPower(depanKiri);
            motorDepanKanan.setPower(depanKanan);
            motorBelakangKiri.setPower(belakangKiri);
            motorBelakangKanan.setPower(belakangKanan);

            telemetry.addData("METODE", namaMetode);
            telemetry.addData("Depan Kiri/Kanan", "%.2f / %.2f", depanKiri, depanKanan);
            telemetry.addData("Belakang Kiri/Kanan", "%.2f / %.2f", belakangKiri, belakangKanan);
            telemetry.update();
        }
        berhenti();
    }

    // ========================================================================
    //   METODE 4 — GESER SEJAUH TARGET (PAKAI ENCODER + STRAFE_MULTIPLIER)
    // ========================================================================
    /**
     * IDENYA:
     *   Sama seperti Auto01 metode 2 (encoder, jalan sejauh jarak
     *   tertentu), tapi buat GESER, dan targetnya SENGAJA dikalikan
     *   STRAFE_MULTIPLIER dulu sebelum diubah jadi tick.
     *
     * KENAPA DIKALIKAN, BUKAN DIBIARKAN APA ADANYA:
     *   Kalau kamu minta robot geser 24 inci TANPA koreksi, dan
     *   ternyata butuh 1.5x lebih banyak putaran roda buat benar-
     *   benar menempuh 24 inci itu (karena slip roller, dijelaskan
     *   di Bagian 1), robot bakal BERHENTI DI TICK YANG "SEHARUSNYA"
     *   24 inci TAPI JARAK ASLINYA CUMA SEKITAR 16 INCI. Mengalikan
     *   target dengan STRAFE_MULTIPLIER SEBELUM dikonversi ke tick
     *   membuat robot "sengaja" menyuruh roda berputar lebih banyak,
     *   supaya jarak FISIK aslinya yang mendekati benar.
     *
     *   Ini konsep KALIBRASI yang sama seperti DIAMETER_RODA_INCI di
     *   Auto01 — bedanya di sana kamu ukur GEOMETRI fisik roda, di
     *   sini kamu ukur PERILAKU SLIP yang cuma bisa diukur lewat
     *   EKSPERIMEN (nggak ada rumus geometri buat menghitungnya).
     */
    private void metode4_GeserBerEncoder(double jarakInci) {

        int targetTick = inciKeTick(jarakInci * STRAFE_MULTIPLIER);

        for (DcMotor m : new DcMotor[]{motorDepanKiri, motorDepanKanan, motorBelakangKiri, motorBelakangKanan}) {
            m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        // Geser ke KANAN: depanKiri & belakangKanan positif, sisanya negatif
        // (lihat rumus di banner atas dengan axial=0, lateral=+KECEPATAN).
        motorDepanKiri.setPower(KECEPATAN);
        motorDepanKanan.setPower(-KECEPATAN);
        motorBelakangKiri.setPower(-KECEPATAN);
        motorBelakangKanan.setPower(KECEPATAN);

        while (opModeIsActive() && posisiRataRataGeser() < targetTick) {
            telemetry.addData("METODE", "4 — Geser Ber-Encoder");
            telemetry.addData("Target (sudah dikali pengali)", "%d tick", targetTick);
            telemetry.addData("Sekarang", "%d tick", posisiRataRataGeser());
            telemetry.addData("Perkiraan jarak asli", "%.1f / %.1f inci",
                    tickKeInci(posisiRataRataGeser()) / STRAFE_MULTIPLIER, jarakInci);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   FUNGSI PEMBANTU
    // ========================================================================

    private double kelilingRoda() {
        return Math.PI * DIAMETER_RODA_INCI;
    }

    private int inciKeTick(double inci) {
        return (int) ((inci / kelilingRoda()) * TICK_PER_PUTARAN);
    }

    private double tickKeInci(int tick) {
        return (tick / TICK_PER_PUTARAN) * kelilingRoda();
    }

    /** Rata-rata nilai MUTLAK keempat encoder — semuanya ikut berputar waktu geser. */
    private int posisiRataRataGeser() {
        return (Math.abs(motorDepanKiri.getCurrentPosition())
                + Math.abs(motorDepanKanan.getCurrentPosition())
                + Math.abs(motorBelakangKiri.getCurrentPosition())
                + Math.abs(motorBelakangKanan.getCurrentPosition())) / 4;
    }

    private void berhenti() {
        motorDepanKiri.setPower(0);
        motorDepanKanan.setPower(0);
        motorBelakangKiri.setPower(0);
        motorBelakangKanan.setPower(0);
    }
}

/*
 * ============================================================================
 *   LATIHAN — kerjakan berurutan
 * ============================================================================
 *
 *  PERCOBAAN 1 — Verifikasi arah semua motor
 *    a. Jalankan metode 1. Keempat roda harus berputar ke arah yang
 *       bikin robot maju LURUS.
 *    b. Kalau ada roda yang muter kebalik: cek pengkabelan motor itu
 *       di config, atau tukar REVERSE/FORWARD di setDirection()-nya.
 *
 *  PERCOBAAN 2 — Rasakan geser murni
 *    a. Jalankan metode 2, tandai posisi awal robot di lantai
 *    b. Ukur seberapa jauh dia geser DAN seberapa banyak dia
 *       "melenceng" maju/mundur (idealnya nol)
 *
 *  PERCOBAAN 3 — Kalibrasi STRAFE_MULTIPLIER
 *    a. Set STRAFE_MULTIPLIER = 1.0 (nonaktifkan koreksinya dulu)
 *    b. Jalankan metode 4 dengan target 24 inci, ukur jarak GESER
 *       ASLINYA pakai meteran
 *    c. Pengali sebenarnya = 24 / (jarak asli yang terukur). Masukkan
 *       angka itu ke STRAFE_MULTIPLIER, jalankan lagi, ukur ulang —
 *       harusnya sekarang jauh lebih dekat ke 24 inci
 *
 *  TANTANGAN
 *    a. Tulis metode5_ProporsionalDiagonal(jarakX, jarakY) yang
 *       menggabungkan ide Auto01 metode 4 (melambat mendekati target)
 *       dengan gerakan diagonal metode 3 — dua sumbu (axial DAN
 *       lateral) melambat BERSAMAAN mendekati target masing-masing.
 * ============================================================================
 */
