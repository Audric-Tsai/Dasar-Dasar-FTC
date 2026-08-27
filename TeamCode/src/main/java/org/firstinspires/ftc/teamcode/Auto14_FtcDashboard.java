package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
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
 *   AUTO FTC DASHBOARD — Tuning Tanpa Deploy Ulang, Grafik Tanpa Batas Kecil
 * ============================================================================
 *
 *   Auto04_PID nyuruh kamu tuning KP/KI/KD dengan cara: ubah angka di
 *   kode -> build -> deploy ke robot -> jalankan -> lihat hasilnya di
 *   layar Driver Station yang KECIL dan cuma nunjukkin ANGKA, bukan
 *   GRAFIK -> ulangi dari awal kalau belum pas. Satu putaran tuning
 *   bisa makan waktu semenit lebih, cuma buat ganti SATU angka.
 *
 *   FTC Dashboard (dibuat tim acmerobotics, dipakai LUAS oleh tim FTC
 *   kompetitif) membenahi DUA hal itu sekaligus:
 *
 *     1. TELEMETRY KE LAPTOP, BUKAN CUMA KE HP DRIVER STATION —
 *        muncul di browser laptop sebagai GRAFIK garis yang bisa
 *        di-zoom, bukan cuma baris angka statis.
 *
 *     2. KONSTANTA BISA DIUBAH DARI BROWSER, SAAT ROBOT LAGI JALAN —
 *        nggak perlu build ulang APK sama sekali buat coba KP yang
 *        beda. Ubah angka di browser, robotnya langsung pakai nilai
 *        baru di loop BERIKUTNYA.
 *
 *   CARA MENGAKSES DASHBOARD (sekali per sesi, di luar kode):
 *     1. Sambungkan laptop ke WiFi Control Hub/Driver Hub
 *     2. Buka browser, ke http://192.168.43.1:8080/dash
 *        (atau IP yang ditampilkan di layar Driver Hub kalau beda)
 *     3. Grafik & kontrol konstanta muncul otomatis begitu OpMode ini
 *        di-INIT
 *
 *   File ini pakai tugas belok PID yang SAMA seperti Auto04/05 —
 *   sengaja dipilih tugas yang SUDAH kamu kenal, supaya yang beda
 *   cuma DASHBOARD-nya, bukan robotnya.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive, imu -> sama Auto01-05
 *
 *   LANJUT KE: Auto15_ArsitekturSubsistem — tuning sekarang cepat,
 *   tapi KODE-nya sendiri (semua 14 file sejauh ini) masih punya
 *   masalah struktural: fungsi yang sama ditulis ulang di tiap file.
 *   Auto15 membenahi itu.
 *
 * ============================================================================
 */

@Config
@Autonomous(name = "Auto FTC Dashboard (Belajar)", group = "Belajar")
public class Auto14_FtcDashboard extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    /**
     * KENAPA KP/KI/KD DI SINI "public static", BUKAN "private static
     * final" SEPERTI SEMUA FILE SEBELUMNYA:
     *
     * @Config di atas class ini bilang ke FTC Dashboard: "pindai
     * class ini, cari field yang PUBLIC dan STATIC, tampilkan
     * sebagai kontrol yang bisa diubah di web UI." Field yang
     * "final" nggak bisa diubah SAMA SEKALI setelah program jalan
     * (itu artinya "final" dalam Java) — jadi field yang mau di-
     * tuning lewat dashboard WAJIB bukan final.
     *
     * Field lain yang TIDAK perlu di-tuning lewat dashboard (macam
     * TOLERANSI_DERAJAT di bawah) TETAP private static final seperti
     * biasa — nggak semua angka perlu bisa diubah live, cuma yang
     * benar-benar mau sering dicoba-coba selagi tuning.
     */
    public static double KP_ARAH = 0.0060;
    public static double KI_ARAH = 0.00003;
    public static double KD_ARAH = 0.0009;

    private static final double INTEGRAL_MAKS_ARAH = 40.0;
    private static final double KECEPATAN_MAKS = 0.5;
    private static final double TOLERANSI_DERAJAT = 1.0;
    private static final double WAKTU_MAKS_DETIK = 4.0;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor motorKiri;
    private DcMotor motorKanan;
    private IMU imu;

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
        metode1_TelemetryBiasa(90);
        //metode2_DenganDashboard(90);
        //metode3_KonstantaBisaDiubah(90);
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(3000);
    }

    // ========================================================================
    //   METODE 1 — TELEMETRY BIASA (CUMA KE DRIVER STATION)
    // ========================================================================
    /**
     * Ini belokPID() persis seperti Auto04/05 — nggak ada yang beda
     * dari sisi ROBOTNYA. Bedanya baru kelihatan kalau kamu buka
     * dashboard di browser SEKARANG: nggak ada apa pun yang muncul
     * di sana, karena telemetry di sini cuma dikirim ke `telemetry`
     * bawaan (layar Driver Station), bukan ke dashboard.
     */
    private void metode1_TelemetryBiasa(double derajatTarget) {
        jalankanBelokPID(derajatTarget, telemetry);
    }

    // ========================================================================
    //   METODE 2 — DENGAN DASHBOARD (TELEMETRY KE DUA-DUANYA)
    // ========================================================================
    /**
     * SATU BARIS YANG BEDA DARI METODE 1:
     *
     *     Telemetry gabungan = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
     *
     *   MultipleTelemetry itu SATU objek yang, waktu dipanggil
     *   addData()/update(), MENERUSKAN panggilan itu ke BEBERAPA
     *   tujuan sekaligus — di sini: layar Driver Station DAN
     *   dashboard.getTelemetry() (yang datanya dikirim lewat WiFi ke
     *   browser). Semua baris addData() di jalankanBelokPID() TIDAK
     *   BERUBAH sama sekali — cuma OBJEK telemetry yang dioper beda.
     *
     *   Buka dashboard di browser SEBELUM tekan PLAY, jalankan
     *   metode ini, lihat grafik "Error" dan "Power" muncul sebagai
     *   GARIS yang bergerak real-time — jauh lebih gampang dibaca
     *   pola osilasi/overshoot-nya dibanding angka yang ganti-ganti
     *   cepat di layar Driver Station.
     */
    private void metode2_DenganDashboard(double derajatTarget) {
        FtcDashboard dashboard = FtcDashboard.getInstance();
        org.firstinspires.ftc.robotcore.external.Telemetry telemetryGabungan =
                new MultipleTelemetry(telemetry, dashboard.getTelemetry());

        jalankanBelokPID(derajatTarget, telemetryGabungan);
    }

    // ========================================================================
    //   METODE 3 — KONSTANTA BISA DIUBAH LIVE DARI BROWSER
    // ========================================================================
    /**
     * KODE-NYA PERSIS SAMA DENGAN METODE 2. Yang beda: KP_ARAH,
     * KI_ARAH, KD_ARAH sekarang muncul sebagai KOTAK ANGKA yang bisa
     * diketik ulang di panel dashboard (bagian "Config"), karena
     * mereka public static dan class ini ditandai @Config.
     *
     * CARA PAKAINYA:
     *   1. Jalankan metode ini
     *   2. Buka tab "Config" di dashboard, cari "Auto14_FtcDashboard"
     *   3. Ubah angka KP_ARAH di sana, tekan Enter
     *   4. LOOP BERIKUTNYA di robot LANGSUNG pakai angka baru itu —
     *      nggak perlu STOP, nggak perlu build ulang, nggak perlu
     *      deploy ulang
     *
     *   Ini yang bikin tuning PID beneran PRAKTIS. Auto04 butuh
     *   puluhan menit buat coba banyak kombinasi KP/KI/KD karena
     *   tiap coba = satu siklus build-deploy. Dengan ini, satu
     *   sesi tuning bisa coba puluhan kombinasi dalam hitungan
     *   menit, karena robotnya nggak perlu berhenti sama sekali di
     *   antara percobaan.
     */
    private void metode3_KonstantaBisaDiubah(double derajatTarget) {
        FtcDashboard dashboard = FtcDashboard.getInstance();
        org.firstinspires.ftc.robotcore.external.Telemetry telemetryGabungan =
                new MultipleTelemetry(telemetry, dashboard.getTelemetry());

        jalankanBelokPID(derajatTarget, telemetryGabungan);
    }

    // ========================================================================
    //   jalankanBelokPID() — Auto04 metode3_PID, apa adanya
    // ========================================================================
    /**
     * Dipakai ulang oleh ketiga metode di atas. Perhatikan parameter
     * `tujuanTelemetry` — fungsi ini nggak tahu dan nggak peduli
     * apakah dia lagi nulis ke Driver Station doang atau ke dua-
     * duanya sekaligus. Dia cuma manggil addData()/update() ke
     * OBJEK yang dikasih — pola ini disebut "dependency injection"
     * kalau mau istilah formalnya: fungsi ini nggak bikin sendiri
     * objek telemetry-nya, dia cuma DIKASIH TAHU mau nulis ke mana.
     */
    private void jalankanBelokPID(double derajatTarget, org.firstinspires.ftc.robotcore.external.Telemetry tujuanTelemetry) {

        double arahTarget = getArah() + derajatTarget;
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

            tujuanTelemetry.addData("KP/KI/KD sekarang", "%.5f / %.6f / %.5f", KP_ARAH, KI_ARAH, KD_ARAH);
            tujuanTelemetry.addData("Error", "%.2f derajat", errorArah);
            tujuanTelemetry.addData("Power", "%.3f", power);
            tujuanTelemetry.update();
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
 *  PERCOBAAN 1 — Lihat grafiknya
 *    a. Sambungkan laptop ke WiFi robot, buka dashboard di browser
 *    b. Jalankan metode 2, perhatikan grafik "Error" dan "Power" di
 *       tab Telemetry
 *    c. Bandingkan segampang apa membaca POLA osilasi di grafik itu
 *       dibanding baca angka yang berubah cepat di layar Driver
 *       Station
 *
 *  PERCOBAAN 2 — Tuning tanpa deploy ulang
 *    a. Jalankan metode 3
 *    b. Sambil robot masih menyala (OpMode belum di-stop), buka tab
 *       Config, ubah KP_ARAH jadi 3x lipat, tekan Enter
 *    c. Jalankan lagi metode 3 (STOP dulu, PLAY lagi — TAPI TANPA
 *       build ulang project). Apa perubahannya kerasa?
 *
 *  PERCOBAAN 3 — Bandingkan waktu tuning
 *    a. Coba cari kombinasi KP_ARAH/KD_ARAH yang paling stabil pakai
 *       metode 3 (ubah lewat dashboard, jalankan ulang, ulangi)
 *    b. Kira-kira berapa lama itu makan waktu, dibanding kalau kamu
 *       harus build+deploy ulang APK tiap coba angka baru?
 *
 *  TANTANGAN
 *    a. Tambah field public static boolean TAMPILKAN_GRAFIK_P (atau
 *       serupa) yang di-@Config, dipakai buat menyalakan/mematikan
 *       sebagian data yang dikirim ke telemetry — berguna kalau
 *       grafiknya kepenuhan terlalu banyak garis buat dibaca
 *    b. Pindahkan KECEPATAN_MAKS dan TOLERANSI_DERAJAT juga jadi
 *       public static (bisa di-tuning), dan pikirkan: angka MANA
 *       yang sebenarnya PANTAS di-tuning lewat dashboard, dan mana
 *       yang lebih aman tetap final (nggak semua angka enak diubah
 *       sembarangan waktu robot lagi jalan)?
 * ============================================================================
 */
