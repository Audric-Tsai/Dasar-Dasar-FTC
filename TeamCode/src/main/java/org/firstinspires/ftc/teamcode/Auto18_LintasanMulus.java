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
 *   AUTO LINTASAN MULUS — Nggak Usah Berhenti di Setiap Sudut Kotak
 * ============================================================================
 *
 *   Auto08 metode 3 (Lintasan) jalan bentuk kotak dengan cara manggil
 *   pergiKeTitik() empat kali berturut-turut — dan tiap panggilan
 *   BENAR-BENAR BERHENTI (melambat sampai nol, cek toleransi ketat)
 *   sebelum lanjut ke titik berikutnya. Robotnya jalan-berhenti-
 *   jalan-berhenti empat kali buat satu kotak.
 *
 *   Tapi coba pikirkan: kenapa harus berhenti PENUH di titik antara?
 *   Titik itu bukan tujuan AKHIR — cuma "belokan" di tengah jalan.
 *   Manusia yang nyetir mobil nggak berhenti total di setiap
 *   tikungan, dia cuma MELAMBAT SECUKUPNYA buat belok, lalu gas lagi.
 *
 *   IDENYA — RADIUS LEWAT (PASS-THROUGH RADIUS):
 *   Buat titik ANTARA (bukan titik terakhir), robot nggak menunggu
 *   sampai PERSIS di titik itu (toleransi ketat) — begitu dia masuk
 *   radius yang agak LONGGAR di sekitar titik itu, dia LANGSUNG
 *   mengarah ke titik BERIKUTNYA, tanpa pernah benar-benar berhenti.
 *   Cuma titik TERAKHIR dalam lintasan yang dapat perlakuan penuh
 *   (melambat + berhenti + toleransi ketat), karena itu tujuan
 *   SEBENARNYA.
 *
 *   CATATAN JUJUR:
 *   Ini pendekatan yang DISEDERHANAKAN, bukan spline/kurva matematis
 *   sungguhan (yang dipakai library seperti Road Runner). Tapi ide
 *   intinya SAMA: jangan buang waktu berhenti total di titik yang
 *   cuma "numpang lewat".
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive         -> drivetrain, sama Auto08
 *     encoder_maju, encoder_geser     -> dead wheel pod, sama Auto07/08
 *     imu                             -> sama semua file sebelumnya
 *
 *   LANJUT KE: Auto19_PIDF — lepas dari drivetrain buat sementara.
 *   Auto17 dan file ini berdua soal MENGHALUSKAN gerakan drivetrain;
 *   Auto19 pindah ke mekanisme yang beda total (lengan) dengan
 *   masalah yang beda total juga (gravitasi, bukan jarak/arah).
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Lintasan Mulus (Belajar)", group = "Belajar")
public class Auto18_LintasanMulus extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    private static final double TICK_PER_PUTARAN_ENCODER = 8192.0;
    private static final double DIAMETER_RODA_ENCODER_INCI = 1.378;
    private static final double OFFSET_GESER_INCI = 6.0;

    private static final double KECEPATAN_MAKS = 0.5;
    /** Power TETAP dipakai buat melewati titik antara — nggak melambat sama sekali. */
    private static final double KECEPATAN_JELAJAH = 0.4;

    private static final double TOLERANSI_JARAK_INCI = 1.0;
    /** Radius "cukup dekat, lanjut ke titik berikutnya" buat titik ANTARA. Longgar, sengaja. */
    private static final double RADIUS_LEWAT_INCI = 6.0;

    private static final double WAKTU_MAKS_DETIK = 6.0;

    private static final double KP_JARAK = 0.05;
    private static final double KI_JARAK = 0.0008;
    private static final double KD_JARAK = 0.01;
    private static final double INTEGRAL_MAKS_JARAK = 10.0;

    private static final double KP_KEJAR = 0.020;
    private static final double KD_KEJAR = 0.004;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor motorKiri;
    private DcMotor motorKanan;
    private DcMotor encoderMaju;
    private DcMotor encoderGeser;
    private IMU imu;

    private double x = 0;
    private double y = 0;
    private int tickMajuSebelumnya = 0;
    private int tickGeserSebelumnya = 0;
    private double headingSebelumnyaRadian = 0;

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

        encoderMaju  = hardwareMap.get(DcMotor.class, "encoder_maju");
        encoderGeser = hardwareMap.get(DcMotor.class, "encoder_geser");
        encoderMaju.setDirection(DcMotor.Direction.FORWARD);
        encoderGeser.setDirection(DcMotor.Direction.REVERSE);
        encoderMaju.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        encoderGeser.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        )));
        imu.resetYaw();
        headingSebelumnyaRadian = getArahRadian();

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_BerhentiTiapTitik();
        //metode2_LewatiTiapTitik();
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.addData("Posisi akhir", "X=%.2f  Y=%.2f", x, y);
        telemetry.update();
        sleep(3000);
    }

    // ========================================================================
    //   METODE 1 — BERHENTI DI SETIAP TITIK (SEPERTI Auto08 metode 3)
    // ========================================================================
    /**
     * Baseline buat perbandingan. Empat panggilan pergiKeTitik(),
     * SEMUANYA pakai perlambatan penuh + toleransi ketat — persis
     * Auto08_Navigasi metode3_Lintasan.
     */
    private void metode1_BerhentiTiapTitik() {

        ElapsedTime waktuTotal = new ElapsedTime();

        pergiKeTitik(24, 0,  TOLERANSI_JARAK_INCI, true);
        pergiKeTitik(24, 24, TOLERANSI_JARAK_INCI, true);
        pergiKeTitik(0, 24,  TOLERANSI_JARAK_INCI, true);
        pergiKeTitik(0, 0,   TOLERANSI_JARAK_INCI, true);

        telemetry.addData("METODE", "1 — Berhenti Tiap Titik");
        telemetry.addData("TOTAL WAKTU", "%.2f detik", waktuTotal.seconds());
        telemetry.update();
    }

    // ========================================================================
    //   METODE 2 — LEWATI TITIK ANTARA (LINTASAN MULUS)
    // ========================================================================
    /**
     * Tiga titik PERTAMA dipanggil dengan radiusSampai LONGGAR
     * (RADIUS_LEWAT_INCI) dan perlambatanAktif = false — robot cuma
     * "menyerempet" dekat titik itu sambil sudah mengarah ke titik
     * berikutnya, TANPA PERNAH benar-benar berhenti (lihat
     * pergiKeTitik(): kalau perlambatanAktif == false, berhenti()
     * TIDAK dipanggil sama sekali di akhir fungsi).
     *
     * Titik TERAKHIR tetap pakai perlambatan penuh + toleransi ketat
     * — itu tujuan SEBENARNYA, harus berhenti presisi di situ.
     */
    private void metode2_LewatiTiapTitik() {

        ElapsedTime waktuTotal = new ElapsedTime();

        pergiKeTitik(24, 0,  RADIUS_LEWAT_INCI, false);
        pergiKeTitik(24, 24, RADIUS_LEWAT_INCI, false);
        pergiKeTitik(0, 24,  RADIUS_LEWAT_INCI, false);
        pergiKeTitik(0, 0,   TOLERANSI_JARAK_INCI, true);

        telemetry.addData("METODE", "2 — Lewati Titik Antara");
        telemetry.addData("TOTAL WAKTU", "%.2f detik", waktuTotal.seconds());
        telemetry.update();
    }

    // ========================================================================
    //   pergiKeTitik() — inti dari dua metode di atas
    // ========================================================================
    /**
     * @param radiusSampai     seberapa dekat dianggap "sampai" — ketat
     *                         (TOLERANSI_JARAK_INCI) buat titik akhir,
     *                         longgar (RADIUS_LEWAT_INCI) buat titik antara
     * @param perlambatanAktif true = power berkurang mendekati target DAN
     *                         motor benar-benar dihentikan di akhir
     *                         (titik akhir). false = power TETAP di
     *                         KECEPATAN_JELAJAH sepanjang perjalanan DAN
     *                         motor TIDAK dihentikan di akhir (titik
     *                         antara — momentum langsung terbawa ke
     *                         panggilan pergiKeTitik() berikutnya)
     */
    private void pergiKeTitik(double targetX, double targetY, double radiusSampai, boolean perlambatanAktif) {

        perbaruiPosisi();

        double integralJarak = 0;
        double errorJarakSebelumnya = Math.hypot(targetX - x, targetY - y);
        double errorArahSebelumnya = 0;

        ElapsedTime waktuTotal = new ElapsedTime();
        ElapsedTime waktuLoop  = new ElapsedTime();

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            perbaruiPosisi();

            double deltaX = targetX - x;
            double deltaY = targetY - y;
            double jarakSisa = Math.hypot(deltaX, deltaY);
            if (jarakSisa <= radiusSampai) break;

            double arahMenujuTarget = Math.toDegrees(Math.atan2(deltaY, deltaX));
            double errorArah = bedakanSudut(arahMenujuTarget, getArahDerajat());

            double dt = Math.max(waktuLoop.seconds(), 0.001);
            waktuLoop.reset();

            double powerDasar;
            if (perlambatanAktif) {
                integralJarak = Range.clip(integralJarak + jarakSisa * dt, -INTEGRAL_MAKS_JARAK, INTEGRAL_MAKS_JARAK);
                double turunanJarak = (jarakSisa - errorJarakSebelumnya) / dt;
                errorJarakSebelumnya = jarakSisa;
                powerDasar = Range.clip(
                        (KP_JARAK * jarakSisa) + (KI_JARAK * integralJarak) + (KD_JARAK * turunanJarak),
                        0, KECEPATAN_MAKS);
            } else {
                powerDasar = KECEPATAN_JELAJAH;
            }

            double turunanArah = (errorArah - errorArahSebelumnya) / dt;
            errorArahSebelumnya = errorArah;
            double koreksi = Range.clip((KP_KEJAR * errorArah) + (KD_KEJAR * turunanArah), -KECEPATAN_MAKS, KECEPATAN_MAKS);

            double powerKiri  = Range.clip(powerDasar - koreksi, -1.0, 1.0);
            double powerKanan = Range.clip(powerDasar + koreksi, -1.0, 1.0);
            motorKiri.setPower(powerKiri);
            motorKanan.setPower(powerKanan);

            telemetry.addData("Menuju", "(%.1f, %.1f)  radius=%.1f  perlambatan=%b", targetX, targetY, radiusSampai, perlambatanAktif);
            telemetry.addData("Posisi", "X=%.2f  Y=%.2f", x, y);
            telemetry.addData("Jarak sisa", "%.2f inci", jarakSisa);
            telemetry.update();
        }

        if (perlambatanAktif) {
            berhenti();
        }
        // Kalau perlambatanAktif == false: SENGAJA nggak dipanggil berhenti().
        // Motor tetap jalan di power yang sedang dikirim, langsung "nyambung"
        // ke perhitungan power BARU di panggilan pergiKeTitik() berikutnya —
        // itu yang bikin gerakannya MULUS, nggak ada jeda berhenti-jalan lagi.
    }

    // ========================================================================
    //   perbaruiPosisi() — persis Auto08_Navigasi
    // ========================================================================
    private void perbaruiPosisi() {

        int tickMajuSekarang  = encoderMaju.getCurrentPosition();
        int tickGeserSekarang = encoderGeser.getCurrentPosition();
        double deltaMaju        = tickKeInciEncoder(tickMajuSekarang - tickMajuSebelumnya);
        double deltaGeserMentah = tickKeInciEncoder(tickGeserSekarang - tickGeserSebelumnya);
        tickMajuSebelumnya  = tickMajuSekarang;
        tickGeserSebelumnya = tickGeserSekarang;

        double headingSekarang = getArahRadian();
        double deltaHeading = bedaSudutRadian(headingSekarang, headingSebelumnyaRadian);
        double headingRataRata = headingSebelumnyaRadian + (deltaHeading / 2.0);
        headingSebelumnyaRadian = headingSekarang;

        double busurPalsu = deltaHeading * OFFSET_GESER_INCI;
        double deltaGeser = deltaGeserMentah - busurPalsu;

        x += deltaMaju * Math.cos(headingRataRata) - deltaGeser * Math.sin(headingRataRata);
        y += deltaMaju * Math.sin(headingRataRata) + deltaGeser * Math.cos(headingRataRata);
    }

    // ========================================================================
    //   FUNGSI PEMBANTU
    // ========================================================================

    private double kelilingRodaEncoder() {
        return Math.PI * DIAMETER_RODA_ENCODER_INCI;
    }

    private double tickKeInciEncoder(int tick) {
        return (tick / TICK_PER_PUTARAN_ENCODER) * kelilingRodaEncoder();
    }

    private double getArahDerajat() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    private double getArahRadian() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
    }

    private double bedakanSudut(double target, double sekarang) {
        double selisih = target - sekarang;
        while (selisih >  180) selisih -= 360;
        while (selisih < -180) selisih += 360;
        return selisih;
    }

    private double bedaSudutRadian(double a, double b) {
        double selisih = a - b;
        while (selisih >  Math.PI) selisih -= 2 * Math.PI;
        while (selisih < -Math.PI) selisih += 2 * Math.PI;
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
 *  PERCOBAAN 1 — Bandingkan waktu total
 *    a. Jalankan metode 1, catat "TOTAL WAKTU"
 *    b. Jalankan metode 2, catat "TOTAL WAKTU". Berapa persen lebih
 *       cepat?
 *
 *  PERCOBAAN 2 — Lihat bentuk lintasannya
 *    a. Kalau memungkinkan, rekam video dari atas (atau minta teman
 *       amati) waktu robot lewat titik (24, 24) di kedua metode
 *    b. Metode 1: robot benar-benar berhenti di situ. Metode 2:
 *       robot "memotong sudut" sedikit — apa itu kelihatan?
 *
 *  PERCOBAAN 3 — Uji radius yang berbeda
 *    a. Ganti RADIUS_LEWAT_INCI jadi 2.0 (sempit), jalankan metode 2
 *    b. Ganti jadi 12.0 (lebar), jalankan lagi
 *    c. Radius lebih lebar = potongan sudut lebih tajam TAPI lebih
 *       cepat, atau sebaliknya? Kenapa?
 *
 *  TANTANGAN
 *    a. RADIUS_LEWAT_INCI sekarang SAMA buat semua titik antara.
 *       Buat radius itu ADAPTIF: hitung sudut antara segmen MASUK
 *       dan segmen KELUAR di tiap titik (pakai atan2 dari titik
 *       sebelumnya, titik ini, dan titik berikutnya) — kalau
 *       belokannya TAJAM (mendekati putar balik), pakai radius
 *       LEBIH BESAR (biar nggak motong kelewat tajam); kalau
 *       belokannya LANDAI, radius KECIL saja cukup.
 * ============================================================================
 */
