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
 *   AUTO PID — Membuka Kotak Hitam di Balik "Kekuatan Koreksi"
 * ============================================================================
 *
 *   Auto02 metode 4 punya baris ini:
 *
 *       double koreksi = error * KEKUATAN_KOREKSI;
 *
 *   ...dan sebuah POWER_MINIMUM yang dipaksakan supaya motor nggak
 *   pernah terlalu lemah buat gerak. Itu HURUF P dari PID, ditambah
 *   tambalan (patch) manual buat masalah yang belum kamu punya nama.
 *
 *   File ini kasih nama masalah itu (STEADY-STATE ERROR), lalu kasih
 *   solusi yang lebih benar daripada sekadar "paksa power minimum":
 *   HURUF I. Lalu satu masalah baru lagi yang muncul, dan HURUF D.
 *
 *   P — sebanding dengan SEBERAPA JAUH dari target sekarang.
 *   I — sebanding dengan SEBERAPA LAMA sudah meleset (akumulasi).
 *   D — sebanding dengan SEBERAPA CEPAT error berubah (mengerem).
 *
 *   Robot yang dipakai untuk belajar: berputar ke suatu sudut,
 *   sama seperti Auto02, karena itu masalah satu sumbu yang paling
 *   gampang dilihat osilasinya.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive, imu -> sama seperti Auto01/02/03
 *
 *   LANJUT KE: Auto05_GabunganPID — TANTANGAN di akhir file ini
 *   minta kamu menulis jalanLurusPID(). Auto05 itu jawabannya:
 *   Auto03 dibangun ulang pakai PID penuh dari file ini.
 *
 * ============================================================================
 */

@Autonomous(name = "Auto PID (Belajar)", group = "Belajar")
public class Auto04_PID extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    /**
     * KP, KI, KD — TIGA "KERAN" YANG MENGATUR SEBERAPA KUAT TIAP HURUF
     *
     * Angka awal di bawah ini SENGAJA konservatif (aman, nggak galak).
     * Robotmu pasti butuh angka berbeda — motor, berat, dan gesekan
     * tiap robot beda-beda. Tuning-nya ada di LATIHAN di bawah.
     *
     * Urutan tuning yang lazim dipakai:
     *   1. KI = KD = 0. Naikkan KP sampai robot mulai OSILASI
     *      (goyang lewat-kurang-lewat-kurang di sekitar target).
     *   2. Turunkan KP dikit (misal 70% dari angka yang bikin osilasi).
     *   3. Naikkan KD pelan-pelan sampai osilasinya hilang/teredam.
     *   4. Kalau masih ada sedikit error yang nggak pernah hilang,
     *      baru naikkan KI SEDIKIT DEMI SEDIKIT.
     */
    private static final double KP = 0.0060;
    private static final double KI = 0.00003;
    private static final double KD = 0.0009;

    /** Batas atas power yang boleh dikeluarkan PID, biar nggak jebol 1.0. */
    private static final double KECEPATAN_MAKS = 0.5;

    /**
     * BATAS INTEGRAL (ANTI-WINDUP)
     *
     * Kalau robot ketahan (mentok tembok, kabel nyangkut, dll), error
     * nggak pernah mengecil, dan integral-nya terus menumpuk TANPA
     * BATAS selama itu. Begitu penghalangnya hilang, robot punya
     * "utang" integral raksasa dan langsung nyelonong jauh lewat
     * target. Ini namanya INTEGRAL WINDUP.
     *
     * Batas ini mencegah integral menumpuk lebih dari nilai ini.
     */
    private static final double INTEGRAL_MAKS = 40.0;

    private static final double TOLERANSI_DERAJAT = 1.0;

    /**
     * WAKTU MAKSIMAL (DETIK)
     *
     * SETIAP loop yang menunggu sesuatu terjadi HARUS punya batas
     * waktu. Kalau nggak, satu sensor yang ngaco bisa menghabiskan
     * seluruh 30 detik autonomous-mu cuma buat nunggu di satu langkah.
     *
     * Di metode 1 (P doang), batas ini juga sekalian jadi BUKTI HIDUP
     * dari steady-state error: kalau P doang nggak pernah cukup kuat
     * buat menutup sisa error yang kecil, loop-nya akan time-out,
     * bukan selesai normal.
     */
    private static final double WAKTU_MAKS_DETIK = 4.0;

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
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_HanyaP(90);
        //metode2_PI(90);
        //metode3_PID(90);
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.addData("Arah akhir", "%.1f derajat", getArah());
        telemetry.update();
        sleep(5000);
    }

    // ========================================================================
    //   METODE 1 — P SAJA, TANPA TAMBALAN POWER_MINIMUM
    // ========================================================================
    /**
     * Ini Auto02 metode 4, DIKURANGI baris POWER_MINIMUM yang dulu
     * dipakai buat "memaksa" motor tetap gerak waktu power-nya kecil.
     *
     *     power = KP x error
     *
     * KENAPA POWER_MINIMUM DULU DIPAKAI:
     *   Waktu error kecil (robot udah deket target), power = KP x
     *   error juga ikut kecil. Kalau kekecilan, motor nggak kuat
     *   lawan gesekan diam (static friction) motor & gearbox-nya
     *   sendiri — dia berhenti gerak SAMA SEKALI walau belum
     *   benar-benar sampai target.
     *
     * INI NAMANYA STEADY-STATE ERROR:
     *   Sistem "settle" (diam, nggak berubah lagi) di suatu titik
     *   yang BUKAN target sebenarnya, karena kekuatan yang dihasilkan
     *   controller di titik itu udah nggak cukup buat gerak lebih
     *   jauh lagi. P doang nggak pernah bisa menutup celah ini
     *   sendirian — errornya harus ADA supaya power-nya ADA, tapi
     *   power yang ada kekecilan buat ngilangin error itu. Buntu.
     *
     * KENAPA LOOP INI PUNYA BATAS WAKTU:
     *   Karena masalah di atas, loop murni P bisa saja NGGAK PERNAH
     *   menyentuh toleransi. Tanpa batas waktu, OpMode-mu macet di
     *   sini selamanya. Lihat "Alasan berhenti" di telemetry setelah
     *   selesai — kalau tertulis TIMEOUT, itu steady-state error
     *   yang kamu baca barusan, kejadian beneran, bukan cuma teori.
     */
    private void metode1_HanyaP(double derajatTarget) {

        double arahTarget = getArah() + derajatTarget;
        ElapsedTime waktuTotal = new ElapsedTime();

        String alasanBerhenti = "TIMEOUT (steady-state error)";

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            double error = bedakanSudut(arahTarget, getArah());
            if (Math.abs(error) <= TOLERANSI_DERAJAT) {
                alasanBerhenti = "SAMPAI TARGET";
                break;
            }

            double power = Range.clip(KP * error, -KECEPATAN_MAKS, KECEPATAN_MAKS);

            motorKiri.setPower(-power);
            motorKanan.setPower(power);

            telemetry.addData("METODE", "1 — P Saja");
            telemetry.addData("Error", "%.2f derajat", error);
            telemetry.addData("Power", "%.3f", power);
            telemetry.addLine();
            telemetry.addLine("Makin dekat target, makin lemah power-nya.");
            telemetry.addLine("Perhatikan apakah dia benar-benar sampai.");
            telemetry.update();
        }

        berhenti();
        telemetry.addData("Alasan berhenti", alasanBerhenti);
        telemetry.update();
    }

    // ========================================================================
    //   METODE 2 — PI (P + INTEGRAL)
    // ========================================================================
    /**
     * TAMBAHAN DARI METODE 1:
     *
     *     integral += error x dt        (dt = waktu sejak loop terakhir)
     *     power = (KP x error) + (KI x integral)
     *
     * KENAPA INI MENYELESAIKAN STEADY-STATE ERROR:
     *   Selama error masih ada (walau kecil), integral TERUS
     *   bertambah tiap loop — dia "mengingat" bahwa robot sudah
     *   lama meleset. Lama-lama, KI x integral jadi cukup besar
     *   buat menambah power sampai motor akhirnya kuat gerak lagi
     *   dan benar-benar mencapai target.
     *
     *   Beda dengan POWER_MINIMUM (paksaan tetap, nggak peduli
     *   keadaan), integral itu ADAPTIF — dia cuma menumpuk kalau
     *   memang masih ada error yang belum selesai.
     *
     * KENAPA ADA INTEGRAL_MAKS:
     *   Lihat penjelasan INTEGRAL_MAKS di Bagian 1. Coba PERCOBAAN 3
     *   di LATIHAN buat lihat sendiri akibatnya kalau batas ini
     *   dihapus.
     */
    private void metode2_PI(double derajatTarget) {

        double arahTarget = getArah() + derajatTarget;
        double integral = 0;

        ElapsedTime waktuTotal = new ElapsedTime();
        ElapsedTime waktuLoop  = new ElapsedTime();

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            double error = bedakanSudut(arahTarget, getArah());
            if (Math.abs(error) <= TOLERANSI_DERAJAT) break;

            double dt = Math.max(waktuLoop.seconds(), 0.001);
            waktuLoop.reset();

            integral = Range.clip(integral + error * dt, -INTEGRAL_MAKS, INTEGRAL_MAKS);

            double power = Range.clip((KP * error) + (KI * integral), -KECEPATAN_MAKS, KECEPATAN_MAKS);

            motorKiri.setPower(-power);
            motorKanan.setPower(power);

            telemetry.addData("METODE", "2 — PI");
            telemetry.addData("Error", "%.2f derajat", error);
            telemetry.addData("Integral", "%.2f", integral);
            telemetry.addData("Power", "%.3f", power);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   METODE 3 — PID (P + INTEGRAL + DERIVATIVE)
    // ========================================================================
    /**
     * MASALAH DI METODE 2:
     *   Coba naikkan KP atau KI cukup tinggi di metode 2 (lihat
     *   PERCOBAAN 2 di LATIHAN). Robot jadi OSILASI — lewat target,
     *   balik lagi, lewat lagi ke arah lain, goyang-goyang sebelum
     *   akhirnya diam. Makin agresif KP/KI, makin parah goyangannya.
     *
     * TAMBAHAN DARI METODE 2:
     *
     *     turunan = (error - errorSebelumnya) / dt
     *     power = (KP x error) + (KI x integral) + (KD x turunan)
     *
     * KENAPA INI MEREDAM OSILASI:
     *   turunan mengukur SEBERAPA CEPAT error sedang berubah — bukan
     *   seberapa besar errornya sekarang. Waktu robot mendekati
     *   target dengan cepat (error mengecil cepat), turunan bernilai
     *   NEGATIF (berlawanan arah gerakan), jadi dia MENGURANGI power.
     *
     *   Efeknya kayak REM yang otomatis nge-gas duluan sebelum
     *   benar-benar nyampe — persis kayak alasan Auto01 metode 4
     *   memperlambat mendekati target JARAK, tapi sekarang otomatis
     *   berdasarkan KECEPATAN error, bukan cuma jaraknya.
     *
     * KENAPA D BISA GALAK KALAU BACAAN SENSOR BERISIK (NOISY):
     *   turunan dihitung dari SELISIH dua bacaan yang berdekatan.
     *   Kalau sensornya "gemetar" dikit aja, selisih itu bisa
     *   melompat-lompat, dan KD x turunan ikut melompat-lompat jadi
     *   power yang gemetar juga. IMU FTC cukup halus buat ini, tapi
     *   ini alasan kenapa KD biasanya angka PALING KECIL dari
     *   ketiganya.
     */
    private void metode3_PID(double derajatTarget) {

        double arahTarget = getArah() + derajatTarget;
        double integral = 0;
        double errorSebelumnya = bedakanSudut(arahTarget, getArah());

        ElapsedTime waktuTotal = new ElapsedTime();
        ElapsedTime waktuLoop  = new ElapsedTime();

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            double error = bedakanSudut(arahTarget, getArah());
            if (Math.abs(error) <= TOLERANSI_DERAJAT) break;

            double dt = Math.max(waktuLoop.seconds(), 0.001);
            waktuLoop.reset();

            integral = Range.clip(integral + error * dt, -INTEGRAL_MAKS, INTEGRAL_MAKS);
            double turunan = (error - errorSebelumnya) / dt;
            errorSebelumnya = error;

            double power = Range.clip(
                    (KP * error) + (KI * integral) + (KD * turunan),
                    -KECEPATAN_MAKS, KECEPATAN_MAKS);

            motorKiri.setPower(-power);
            motorKanan.setPower(power);

            telemetry.addData("METODE", "3 — PID");
            telemetry.addData("Error", "%.2f derajat", error);
            telemetry.addData("P", "%.3f", KP * error);
            telemetry.addData("I", "%.3f", KI * integral);
            telemetry.addData("D", "%.3f", KD * turunan);
            telemetry.addData("Power total", "%.3f", power);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   FUNGSI PEMBANTU
    // ========================================================================

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
 *  PERCOBAAN 1 — Lihat steady-state error beneran
 *    a. Jalankan metode 1 target 90 derajat
 *    b. Baca "Alasan berhenti" di telemetry setelah OpMode selesai
 *    c. Kalau tertulis TIMEOUT, catat error terakhirnya. Itu bukti
 *       robotmu berhenti gerak sebelum sampai target.
 *
 *  PERCOBAAN 2 — Buat robot osilasi (sengaja)
 *    a. Di metode 2, naikkan KP jadi 3x lipat
 *    b. Jalankan. Robotnya goyang lewat-kurang di sekitar target?
 *    c. Kembalikan KP ke angka semula
 *
 *  PERCOBAAN 3 — Lihat integral windup
 *    a. Di metode 2, ganti INTEGRAL_MAKS jadi angka sangat besar
 *       (misal 100000) — sama saja meniadakan batasnya
 *    b. Selama OpMode INIT (sebelum PLAY), tahan robot pakai tangan
 *       supaya nggak bisa muter
 *    c. Tekan PLAY, biarkan tertahan tangan beberapa detik, baru
 *       lepas. Robotnya nyelonong jauh lewat target?
 *    d. Kembalikan INTEGRAL_MAKS ke angka semula
 *
 *  PERCOBAAN 4 — Tuning beneran
 *    a. Ikuti urutan tuning di komentar KP/KI/KD di Bagian 1
 *    b. Catat angka KP, KI, KD yang paling pas buat ROBOTMU sendiri
 *       (robot lain hampir pasti butuh angka berbeda)
 *
 *  TANTANGAN
 *    a. Ganti target dari sudut (derajat, dari Auto02) jadi JARAK
 *       (inci, dari Auto01). Tulis jalanLurusPID(inci) yang memakai
 *       pola PID yang sama seperti metode3_PID di atas, tapi
 *       mengoreksi jarak alih-alih sudut.
 * ============================================================================
 */
