package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * ============================================================================
 *   AUTO PIDF — Melawan Gravitasi yang SELALU Ada, Bukan Cuma Kadang-Kadang
 * ============================================================================
 *
 *   Auto04 mengajarkan P, I, D lewat robot MUTER — beban yang
 *   dilawan di situ (gesekan motor/gearbox) kira-kira SAMA di semua
 *   sudut. File ini pakai LENGAN yang berputar di bidang TEGAK
 *   (naik-turun, misalnya lengan angkat/intake), di mana beban yang
 *   dilawan itu GRAVITASI — dan gravitasi BUKAN beban yang konstan,
 *   dia berubah tergantung SUDUT lengan:
 *
 *     Lengan MENDATAR (horizontal)  -> torsi gravitasi PALING BESAR
 *     Lengan TEGAK LURUS (vertikal) -> torsi gravitasi NOL
 *
 *   (Bayangkan pegang barbel dengan lengan lurus ke samping vs lurus
 *   ke atas — yang pertama jauh lebih berat dirasakan, walau
 *   barbelnya sama.)
 *
 *   HURUF F — FEEDFORWARD:
 *   Auto17 sudah memperkenalkan ide "hitung power dari RENCANA,
 *   bukan cuma dari ERROR". Di sini, F itu power yang dihitung
 *   LANGSUNG dari SUDUT LENGAN SEKARANG (bukan dari target, bukan
 *   dari error) — sebuah TEBAKAN TERDIDIK tentang "seberapa banyak
 *   power yang PASTI dibutuhkan cuma buat melawan gravitasi di sudut
 *   ini", dihitung SEBELUM PID sempat berbuat apa-apa:
 *
 *     F = KF x cos(sudutLengan)
 *
 *   PID (P+I+D) di atasnya cuma bertugas membetulkan SISA kesalahan
 *   kecil — bukan menanggung SELURUH beban melawan gravitasi
 *   sendirian, seperti yang terjadi di metode 1 dan 2 di bawah.
 *
 *   ROBOT INI PAKAI:
 *     motor_lengan -> DcMotor + encoder, lengan berputar di bidang
 *                     tegak (naik-turun), sudut 0 = mendatar
 *
 * ============================================================================
 */

@Autonomous(name = "Auto PIDF (Belajar)", group = "Belajar")
public class Auto18_PIDF extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    /**
     * Tick per putaran POROS LENGAN (bukan poros motor mentah) —
     * sudah termasuk gearbox internal DAN reduksi eksternal kalau
     * ada. UKUR SENDIRI: putar lengan manual tepat 360 derajat,
     * catat berapa tick yang terbaca.
     */
    private static final double TICK_PER_PUTARAN_LENGAN = 1993.6; // CONTOH, UKUR SENDIRI

    /** Sudut lengan waktu OpMode di-INIT, sebelum encoder di-reset. 0 = mendatar. */
    private static final double SUDUT_AWAL_DERAJAT = 0.0;

    private static final double KECEPATAN_MAKS = 0.6;
    private static final double TOLERANSI_DERAJAT = 2.0;
    private static final double WAKTU_MAKS_DETIK = 5.0;

    private static final double KP = 0.02;
    private static final double KI = 0.0005;
    private static final double KD = 0.001;
    private static final double INTEGRAL_MAKS = 30.0;

    /**
     * KF — power yang dibutuhkan buat MENAHAN lengan diam waktu dia
     * MENDATAR (sudut = 0, torsi gravitasi PALING BESAR).
     *
     * CARA UKUR: lepas semua kontrol PID (matikan motor), pegang
     * lengan mendatar pakai tangan, lalu naikkan power motor
     * pelan-pelan (tanpa PID, cuma setPower manual) sampai lengan
     * PERSIS berhenti jatuh/naik sendiri. Angka power itulah KF.
     */
    private static final double KF = 0.18; // CONTOH, UKUR SENDIRI

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor motorLengan;

    // ========================================================================
    //   BAGIAN 3 — PROGRAM UTAMA
    // ========================================================================

    @Override
    public void runOpMode() {

        motorLengan = hardwareMap.get(DcMotor.class, "motor_lengan");
        motorLengan.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorLengan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorLengan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.addLine("Pastikan lengan bertumpu di posisi SUDUT_AWAL_DERAJAT sebelum PLAY.");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_HanyaP(60);
        //metode2_PID(60);
        //metode3_PIDF(60);
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.addData("Sudut akhir", "%.1f derajat", getSudutLengan());
        telemetry.update();
        sleep(3000);
    }

    // ========================================================================
    //   METODE 1 — P SAJA (SAGGING KARENA GRAVITASI, BUKAN GESEKAN)
    // ========================================================================
    /**
     * Ini pola yang SAMA seperti Auto04 metode 1 (P doang, timeout
     * sebagai bukti steady-state error) — TAPI penyebabnya BEDA.
     *
     * Di Auto04, penyebabnya GESEKAN INTERNAL motor/gearbox — beban
     * yang kira-kira konstan di semua sudut. Di sini, penyebabnya
     * GRAVITASI — beban yang TERUS MENARIK lengan turun, bahkan
     * waktu lengan sudah "sampai" target. P doang cuma menghasilkan
     * power kalau ADA error, jadi lengan akan SELALU nyangkut di
     * bawah target (sagging) sejauh yang dibutuhkan buat menghasilkan
     * cukup power melawan gravitasi — dia nggak akan pernah benar-
     * benar "istirahat" di angka error 0 selama gravitasi masih
     * menarik.
     */
    private void metode1_HanyaP(double sudutTarget) {

        ElapsedTime waktuTotal = new ElapsedTime();
        String alasanBerhenti = "TIMEOUT (sagging karena gravitasi)";

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            double error = sudutTarget - getSudutLengan();
            if (Math.abs(error) <= TOLERANSI_DERAJAT) {
                alasanBerhenti = "SAMPAI TARGET";
                break;
            }

            double power = Range.clip(KP * error, -KECEPATAN_MAKS, KECEPATAN_MAKS);
            motorLengan.setPower(power);

            telemetry.addData("METODE", "1 — P Saja");
            telemetry.addData("Target", "%.1f derajat", sudutTarget);
            telemetry.addData("Sekarang", "%.1f derajat", getSudutLengan());
            telemetry.addData("Power", "%.3f", power);
            telemetry.update();
        }

        motorLengan.setPower(0);
        telemetry.addData("Alasan berhenti", alasanBerhenti);
        telemetry.update();
    }

    // ========================================================================
    //   METODE 2 — PID PENUH (MENGATASI SAGGING, TAPI LAMBAT & BISA OVERSHOOT)
    // ========================================================================
    /**
     * Tambah I: selama masih ada error (lengan sagging di bawah
     * target), integral terus menumpuk sampai akhirnya CUKUP besar
     * buat menghasilkan power ekstra yang melawan gravitasi. Ini
     * MENGHILANGKAN sagging, TAPI ada harga yang dibayar:
     *
     *   - LAMBAT nyampe: integral butuh WAKTU buat "menumpuk" cukup
     *     tinggi. Selama itu, lengan tertahan di bawah target lebih
     *     lama dibanding kalau gravitasinya langsung dikompensasi.
     *   - OVERSHOOT: begitu integral akhirnya cukup besar buat
     *     menutup gravitasi, dia SERING kelebihan (karena "telat"
     *     terkumpul), bikin lengan lewat target sebelum akhirnya
     *     turun lagi meredam.
     *
     * Ini masalah yang KHAS buat mekanisme melawan gravitasi —
     * makanya feedforward (metode 3) jauh lebih disukai buat
     * lengan/lift dibanding cuma mengandalkan I.
     */
    private void metode2_PID(double sudutTarget) {

        double integral = 0;
        double errorSebelumnya = sudutTarget - getSudutLengan();

        ElapsedTime waktuTotal = new ElapsedTime();
        ElapsedTime waktuLoop  = new ElapsedTime();

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            double error = sudutTarget - getSudutLengan();
            if (Math.abs(error) <= TOLERANSI_DERAJAT) break;

            double dt = Math.max(waktuLoop.seconds(), 0.001);
            waktuLoop.reset();

            integral = Range.clip(integral + error * dt, -INTEGRAL_MAKS, INTEGRAL_MAKS);
            double turunan = (error - errorSebelumnya) / dt;
            errorSebelumnya = error;

            double power = Range.clip(
                    (KP * error) + (KI * integral) + (KD * turunan),
                    -KECEPATAN_MAKS, KECEPATAN_MAKS);

            motorLengan.setPower(power);

            telemetry.addData("METODE", "2 — PID Penuh");
            telemetry.addData("Target", "%.1f derajat", sudutTarget);
            telemetry.addData("Sekarang", "%.1f derajat", getSudutLengan());
            telemetry.addData("Integral", "%.2f", integral);
            telemetry.addData("Power", "%.3f", power);
            telemetry.update();
        }

        motorLengan.setPower(0);
    }

    // ========================================================================
    //   METODE 3 — PIDF (FEEDFORWARD + PID)
    // ========================================================================
    /**
     * TAMBAHAN DARI METODE 2:
     *
     *     F = KF x cos(sudutLengan SEKARANG)
     *     power = F + (KP x error) + (KI x integral) + (KD x turunan)
     *
     *   F dihitung dari SUDUT SEKARANG (bukan target, bukan error) —
     *   dia nggak peduli lengan lagi menuju ke mana, cuma peduli
     *   "di sudut ini, gravitasi butuh dilawan sebesar apa RIGHT NOW".
     *   Karena itu F LANGSUNG memberi sebagian besar power yang
     *   dibutuhkan SEJAK LOOP PERTAMA — PID tinggal membetulkan sisa
     *   kecil, bukan menanggung semuanya dari nol kayak metode 1/2.
     *
     * KENAPA KI SERING BISA DIKECILKAN BANYAK DI SINI:
     *   Kalau F sudah menghitung gravitasi dengan akurat, PID
     *   hampir nggak pernah ketemu error besar yang menetap — jadi
     *   integral hampir nggak pernah perlu menumpuk banyak. Beberapa
     *   tim bahkan set KI = 0 sama sekali buat lengan kalau F-nya
     *   sudah dikalibrasi bagus.
     *
     * KENAPA F DIHITUNG ULANG TIAP LOOP, BUKAN SEKALI DI AWAL:
     *   Torsi gravitasi BERUBAH seiring lengan bergerak (mendekati
     *   vertikal = makin kecil). F yang dihitung dari sudut SEKARANG
     *   tiap loop otomatis mengikuti perubahan itu — F di awal
     *   gerakan (lengan masih mendatar) beda dengan F di akhir
     *   (lengan sudah lebih tegak), dan itu BENAR, bukan bug.
     */
    private void metode3_PIDF(double sudutTarget) {

        double integral = 0;
        double errorSebelumnya = sudutTarget - getSudutLengan();

        ElapsedTime waktuTotal = new ElapsedTime();
        ElapsedTime waktuLoop  = new ElapsedTime();

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            double error = sudutTarget - getSudutLengan();
            if (Math.abs(error) <= TOLERANSI_DERAJAT) break;

            double dt = Math.max(waktuLoop.seconds(), 0.001);
            waktuLoop.reset();

            integral = Range.clip(integral + error * dt, -INTEGRAL_MAKS, INTEGRAL_MAKS);
            double turunan = (error - errorSebelumnya) / dt;
            errorSebelumnya = error;

            double feedforward = KF * Math.cos(Math.toRadians(getSudutLengan()));
            double pid = (KP * error) + (KI * integral) + (KD * turunan);
            double power = Range.clip(feedforward + pid, -KECEPATAN_MAKS, KECEPATAN_MAKS);

            motorLengan.setPower(power);

            telemetry.addData("METODE", "3 — PIDF");
            telemetry.addData("Target", "%.1f derajat", sudutTarget);
            telemetry.addData("Sekarang", "%.1f derajat", getSudutLengan());
            telemetry.addData("F (gravitasi)", "%.3f", feedforward);
            telemetry.addData("PID (koreksi)", "%.3f", pid);
            telemetry.addData("Power total", "%.3f", power);
            telemetry.update();
        }

        motorLengan.setPower(0);
    }

    // ========================================================================
    //   FUNGSI PEMBANTU
    // ========================================================================

    private double getSudutLengan() {
        return SUDUT_AWAL_DERAJAT + (motorLengan.getCurrentPosition() / TICK_PER_PUTARAN_LENGAN) * 360.0;
    }
}

/*
 * ============================================================================
 *   LATIHAN — kerjakan berurutan
 * ============================================================================
 *
 *  PERCOBAAN 1 — Ukur KF dengan benar
 *    a. Ikuti prosedur di komentar KF (Bagian 1) buat menemukan
 *       angka KF robotmu sendiri
 *    b. Coba juga di sudut LAIN (bukan cuma mendatar) — apa power
 *       yang dibutuhkan buat menahan diam beda-beda sesuai sudut?
 *       Itu bukti kenapa F pakai cos(sudut), bukan angka tetap.
 *
 *  PERCOBAAN 2 — Lihat sagging metode 1
 *    a. Jalankan metode 1 target 60 derajat, catat "Alasan berhenti"
 *       dan sudut akhirnya
 *    b. Seberapa jauh dari 60 derajat dia "nyangkut"?
 *
 *  PERCOBAAN 3 — Rasakan lambatnya integral
 *    a. Jalankan metode 2 target yang sama, ukur berapa detik sampai
 *       akhirnya masuk toleransi
 *    b. Perhatikan apa dia overshoot (lewat 60 derajat) sebelum
 *       akhirnya stabil
 *
 *  PERCOBAAN 4 — Buktikan PIDF jauh lebih cepat
 *    a. Jalankan metode 3, ukur waktunya
 *    b. Bandingkan ketiga metode: waktu sampai target DAN seberapa
 *       halus (nggak overshoot) pergerakannya
 *
 *  TANTANGAN
 *    a. Set KI = 0 di metode 3 (matikan integral sepenuhnya). Kalau
 *       KF-mu terkalibrasi dengan baik, apa lengan tetap bisa sampai
 *       target dengan akurat, cuma pakai PD + F?
 *    b. Gabungkan dengan Auto17: rencanakan profil KECEPATAN SUDUT
 *       (bukan power langsung dari error), lalu feedforward = F
 *       (gravitasi) + kecepatan rencana / kecepatan maks — dua jenis
 *       feedforward digabung sekaligus.
 * ============================================================================
 */
