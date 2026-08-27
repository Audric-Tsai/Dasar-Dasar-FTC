package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * ============================================================================
 *   AUTO GABUNGAN PID — Auto03 + Auto04, Jadi Satu
 * ============================================================================
 *
 *   Ini jawaban dari TANTANGAN di akhir Auto04_PID:
 *
 *       "Ganti target dari sudut (derajat, dari Auto02) jadi JARAK
 *        (inci, dari Auto01). Tulis jalanLurusPID(inci) yang
 *        memakai pola PID yang sama seperti metode3_PID."
 *
 *   Dua fungsi di file ini gantiin dua fungsi senama di Auto03:
 *
 *     jalanLurusPID(inci)  = Auto03 jalanLurus(), TAPI bagian
 *                             "berhenti pelan-pelan"-nya (dulu cuma
 *                             P + POWER_MINIMUM) sekarang PID PENUH
 *                             (P + I + D) seperti Auto04. Bagian
 *                             jaga-arah-lurus TETAP proporsional
 *                             biasa — itu bukan yang lagi di-upgrade.
 *
 *     belokPID(derajat)    = PERSIS metode3_PID di Auto04, cuma
 *                             dipindah jadi fungsi biasa (bukan
 *                             "metode demo") supaya bisa dipanggil
 *                             berkali-kali kayak belok() di Auto03.
 *
 *   KEUNTUNGAN NYATA DIBANDING Auto03:
 *   Auto03 pakai power_dasar yang di-clip supaya nggak pernah negatif
 *   — jadi kalau robot KEBABLASAN lewat target dikit (misalnya
 *   ketendang), dia nggak pernah bisa MUNDUR buat koreksi balik.
 *   Versi PID di sini BISA, karena error negatif otomatis
 *   menghasilkan power negatif (mundur).
 *
 *   Robot di bawah jalan bentuk KOTAK yang SAMA seperti Auto03 —
 *   bandingkan kehalusan berhentinya kalau kamu punya keduanya
 *   ditulis berdampingan.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive, imu -> sama seperti Auto01-04
 *
 *   LANJUT KE: Auto06_Servo — ini file terakhir yang cuma soal
 *   drivetrain buat sementara. Auto06 pindah ke aktuator yang SAMA
 *   SEKALI beda cara kerjanya: servo, yang nggak punya encoder sama
 *   sekali.
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Gabungan PID (Belajar)", group = "Belajar")
public class Auto05_GabunganPID extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    private static final double TICK_PER_PUTARAN   = 560.0;
    private static final double DIAMETER_RODA_INCI = 3.54;

    /** Batas atas power hasil PID, biar nggak jebol 1.0. */
    private static final double KECEPATAN_MAKS = 0.5;

    /** Dari Auto03 — kekuatan koreksi jaga-arah-lurus. TIDAK di-PID-kan. */
    private static final double KEKUATAN_KOREKSI_LURUS = 0.03;

    /**
     * PID JARAK (dipakai jalanLurusPID)
     * Angka awal — tuning ulang buat robotmu, lihat urutan tuning
     * di komentar Auto04_PID Bagian 1.
     */
    private static final double KP_JARAK = 0.05;
    private static final double KI_JARAK = 0.0008;
    private static final double KD_JARAK = 0.01;
    private static final double INTEGRAL_MAKS_JARAK = 10.0;
    private static final double TOLERANSI_INCI = 0.5;

    /** PID ARAH (dipakai belokPID) — sama seperti Auto04. */
    private static final double KP_ARAH = 0.0060;
    private static final double KI_ARAH = 0.00003;
    private static final double KD_ARAH = 0.0009;
    private static final double INTEGRAL_MAKS_ARAH = 40.0;
    private static final double TOLERANSI_DERAJAT = 1.0;

    /** Setiap loop tunggu HARUS punya batas waktu. Lihat Auto04. */
    private static final double WAKTU_MAKS_DETIK = 5.0;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor motorKiri;
    private DcMotor motorKanan;
    private IMU     imu;

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

        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        )));
        imu.resetYaw();

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.addLine("Robot akan jalan bentuk KOTAK 24x24 inci, versi PID.");
        telemetry.update();

        waitForStart();

        langkah("1/8 — Maju (PID)");  jalanLurusPID(24);
        langkah("2/8 — Belok (PID)"); belokPID(90);
        langkah("3/8 — Maju (PID)");  jalanLurusPID(24);
        langkah("4/8 — Belok (PID)"); belokPID(90);
        langkah("5/8 — Maju (PID)");  jalanLurusPID(24);
        langkah("6/8 — Belok (PID)"); belokPID(90);
        langkah("7/8 — Maju (PID)");  jalanLurusPID(24);
        langkah("8/8 — Belok (PID)"); belokPID(90);

        telemetry.addLine("SELESAI — seharusnya kembali ke titik awal.");
        telemetry.addData("Arah akhir", "%.1f derajat (target: 0)", getArah());
        telemetry.update();
        sleep(5000);
    }

    private void langkah(String nama) {
        telemetry.addData("LANGKAH", nama);
        telemetry.update();
    }

    // ========================================================================
    //   jalanLurusPID() — Auto03 jalanLurus() + PID Auto04
    // ========================================================================
    /**
     * Dua controller jalan BERSAMAAN, masing-masing urus hal beda:
     *
     *   powerDasar -> PID PENUH atas error JARAK. Ini yang berubah
     *                 dari Auto03: bukan cuma P + lantai minimum,
     *                 sekarang P+I+D beneran, lengkap dengan
     *                 kemampuan MUNDUR kalau kebablasan.
     *
     *   koreksi    -> proporsional biasa atas error ARAH, PERSIS
     *                 seperti Auto03. Nggak semua hal butuh PID
     *                 penuh — koreksi arah kecil yang terus-menerus
     *                 kayak gini nggak menunjukkan gejala steady-
     *                 state error atau osilasi separah kontrol
     *                 jarak/sudut yang "mengejar target lalu
     *                 berhenti", jadi P doang sudah cukup di sini.
     */
    private void jalanLurusPID(double jarakInci) {

        double arahTarget = getArah();

        double integralJarak = 0;
        double errorJarakSebelumnya = jarakInci;

        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        ElapsedTime waktuTotal = new ElapsedTime();
        ElapsedTime waktuLoop  = new ElapsedTime();

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            double errorJarak = jarakInci - tickKeInci(posisiRataRata());
            if (Math.abs(errorJarak) <= TOLERANSI_INCI) break;

            double dt = Math.max(waktuLoop.seconds(), 0.001);
            waktuLoop.reset();

            integralJarak = Range.clip(integralJarak + errorJarak * dt, -INTEGRAL_MAKS_JARAK, INTEGRAL_MAKS_JARAK);
            double turunanJarak = (errorJarak - errorJarakSebelumnya) / dt;
            errorJarakSebelumnya = errorJarak;

            double powerDasar = Range.clip(
                    (KP_JARAK * errorJarak) + (KI_JARAK * integralJarak) + (KD_JARAK * turunanJarak),
                    -KECEPATAN_MAKS, KECEPATAN_MAKS);

            double errorArah = bedakanSudut(arahTarget, getArah());
            double koreksi = errorArah * KEKUATAN_KOREKSI_LURUS;

            double powerKiri  = Range.clip(powerDasar - koreksi, -1.0, 1.0);
            double powerKanan = Range.clip(powerDasar + koreksi, -1.0, 1.0);

            motorKiri.setPower(powerKiri);
            motorKanan.setPower(powerKanan);

            telemetry.addData("jalanLurusPID", "%.1f / %.1f inci", tickKeInci(posisiRataRata()), jarakInci);
            telemetry.addData("Power dasar (PID)", "%.3f", powerDasar);
            telemetry.addData("Koreksi arah (P)", "%.3f", koreksi);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   belokPID() — Auto04 metode3_PID, apa adanya
    // ========================================================================
    /** Persis metode3_PID di Auto04_PID. Lihat file itu untuk
     *  penjelasan lengkap kenapa tiap suku P/I/D ada. */
    private void belokPID(double derajatRelatif) {

        double arahTarget = getArah() + derajatRelatif;
        double integralArah = 0;
        double errorArahSebelumnya = bedakanSudut(arahTarget, getArah());

        ElapsedTime waktuTotal = new ElapsedTime();
        ElapsedTime waktuLoop  = new ElapsedTime();

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            double errorArah = bedakanSudut(arahTarget, getArah());
            if (Math.abs(errorArah) <= TOLERANSI_DERAJAT) break;

            double dt = Math.max(waktuLoop.seconds(), 0.001);
            waktuLoop.reset();

            integralArah = Range.clip(integralArah + errorArah * dt, -INTEGRAL_MAKS_ARAH, INTEGRAL_MAKS_ARAH);
            double turunanArah = (errorArah - errorArahSebelumnya) / dt;
            errorArahSebelumnya = errorArah;

            double power = Range.clip(
                    (KP_ARAH * errorArah) + (KI_ARAH * integralArah) + (KD_ARAH * turunanArah),
                    -KECEPATAN_MAKS, KECEPATAN_MAKS);

            motorKiri.setPower(-power);
            motorKanan.setPower(power);

            telemetry.addData("belokPID", "%.1f -> %.1f derajat", getArah(), arahTarget);
            telemetry.addData("Power (PID)", "%.3f", power);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   FUNGSI PEMBANTU — sama seperti Auto01/02/03
    // ========================================================================

    private double kelilingRoda() {
        return Math.PI * DIAMETER_RODA_INCI;
    }

    private double tickKeInci(int tick) {
        return (tick / TICK_PER_PUTARAN) * kelilingRoda();
    }

    private int posisiRataRata() {
        return (Math.abs(motorKiri.getCurrentPosition())
                + Math.abs(motorKanan.getCurrentPosition())) / 2;
    }

    private double getArah() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    private double bedakanSudut(double target, double sekarang) {
        double selisih = target - sekarang;
        while (selisih >  180) selisih -= 360;
        while (selisih < -180) selisih += 360;
        return selisih;
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
 *  PERCOBAAN 1 — Bandingkan langsung dengan Auto03
 *    a. Jalankan Auto03_Gabungan, rekam videonya (atau perhatikan
 *       baik-baik) waktu robot berhenti di ujung tiap sisi kotak
 *    b. Jalankan file ini, bandingkan. Lebih halus? Lebih cepat
 *       sampai target?
 *
 *  PERCOBAAN 2 — Buktikan kemampuan koreksi mundur
 *    a. Jalankan jalanLurusPID(24) sendirian (bikin OpMode
 *       sederhana atau modifikasi langkah di atas)
 *    b. Waktu robot hampir sampai, DORONG dia pelan-pelan lewat
 *       target pakai tangan
 *    c. Robotnya mundur sendiri buat koreksi? Auto03 nggak bisa
 *       melakukan ini.
 *
 *  PERCOBAAN 3 — Tuning PID jarak
 *    a. Ikuti urutan tuning yang sama seperti Auto04 (naikkan
 *       KP_JARAK sampai osilasi, turunkan, tambah KD_JARAK, baru
 *       KI_JARAK kalau perlu)
 *    b. Catat angka yang paling pas buat robotmu
 *
 *  TANTANGAN
 *    a. Sekarang gabungkan SEKALIGUS: bikin koreksi arah di
 *       jalanLurusPID() jadi PID PENUH juga (bukan cuma P), pakai
 *       KP_ARAH/KI_ARAH/KD_ARAH yang sudah ada. Apakah hasilnya
 *       terasa lebih baik daripada versi P-doang? Atau malah lebih
 *       susah di-tuning tanpa manfaat yang kelihatan? Ini pertanyaan
 *       nyata yang tim FTC beneran hadapi: PID PENUH nggak selalu
 *       lebih baik dari P sederhana buat SETIAP bagian sistem.
 * ============================================================================
 */
