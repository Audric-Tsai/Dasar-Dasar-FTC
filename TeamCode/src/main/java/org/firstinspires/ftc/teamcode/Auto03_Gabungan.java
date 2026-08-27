package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * ============================================================================
 *   AUTO GABUNGAN — Menyusun Autonomous Sungguhan dari Auto01 + Auto02
 * ============================================================================
 *
 *   "Gabungan" = gabungan/kombinasi. File ini nggak mengajarkan konsep
 *   baru — dia menggabungkan metode TERBAIK dari dua pelajaran sebelumnya
 *   jadi dua fungsi siap pakai:
 *
 *     jalanLurus(inci)   = Auto01 metode 4 (pelan-pelan berhenti)
 *                           DIGABUNG DENGAN metode 5 (dijaga lurus pakai IMU)
 *     belok(derajat)     = Auto02 metode 4 (IMU proporsional)
 *
 *   Ini persis "TANTANGAN" di akhir Auto01 dan Auto02. Kalau kamu sudah
 *   coba mengerjakannya sendiri, bandingkan hasilmu dengan file ini.
 *
 *   Begitu dua fungsi ini ada, autonomous jadi gampang dibaca — cuma
 *   daftar perintah, kayak resep:
 *
 *       jalanLurus(24);
 *       belok(90);
 *       jalanLurus(24);
 *       ...
 *
 *   Robot di bawah akan jalan bentuk KOTAK: maju, belok 90 derajat,
 *   empat kali berturut-turut, lalu berhenti kira-kira di titik awal.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive, imu -> sama seperti Auto01 & Auto02
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Gabungan (Belajar)", group = "Belajar")
public class Auto03_Gabungan extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================
    // Sama seperti Auto01 & Auto02. Kalau kamu sudah kalibrasi di sana,
    // salin angkanya ke sini juga.

    private static final double TICK_PER_PUTARAN   = 560.0;
    private static final double DIAMETER_RODA_INCI = 3.54;

    private static final double KECEPATAN        = 0.3;   // buat jalanLurus()
    private static final double KECEPATAN_BELOK  = 0.3;   // buat belok()
    private static final double POWER_MINIMUM    = 0.15;

    /** Dari Auto01 metode 5 — kekuatan koreksi menjaga arah tetap lurus. */
    private static final double KEKUATAN_KOREKSI_LURUS = 0.03;

    private static final double TOLERANSI_DERAJAT = 1.0;

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
        telemetry.addLine("Robot akan jalan bentuk KOTAK 24x24 inci.");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   INI DIA — SEBUAH AUTONOMOUS SUNGGUHAN.
        //
        //   Nggak ada lagi loop panjang penuh matematika di sini. Semua
        //   kerumitan itu sudah "dikubur" di dalam jalanLurus() dan
        //   belok(). Ini bagian yang biasanya kamu tunjukkan ke orang
        //   lain kalau ditanya "kode autonomous kamu gimana?".
        //
        // ====================================================================

        langkah("1/8 — Maju");   jalanLurus(24);
        langkah("2/8 — Belok");  belok(90);
        langkah("3/8 — Maju");   jalanLurus(24);
        langkah("4/8 — Belok");  belok(90);
        langkah("5/8 — Maju");   jalanLurus(24);
        langkah("6/8 — Belok");  belok(90);
        langkah("7/8 — Maju");   jalanLurus(24);
        langkah("8/8 — Belok");  belok(90);

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
    //   jalanLurus() — Auto01 metode 4 + metode 5 digabung
    // ========================================================================
    /**
     * DUA KOREKSI SEKALIGUS, DUA ALASAN BERBEDA:
     *
     *   powerDasar  -> berkurang mendekati target JARAK (metode 4).
     *                  Ini soal KAPAN dan SEBERAPA CEPAT berhenti.
     *
     *   koreksi     -> ditambah/dikurang dari powerDasar berdasarkan
     *                  error ARAH terhadap IMU (metode 5). Ini soal
     *                  KE MANA robot menghadap SELAMA perjalanan.
     *
     *   Dua hal ini independen satu sama lain — itu sebabnya bisa
     *   digabung dengan cara sesederhana "tambah/kurang". powerDasar
     *   menentukan seberapa cepat DUA roda sama-sama jalan; koreksi
     *   menentukan seberapa BEDA power kiri vs kanan.
     *
     *   arahTarget diambil dari getArah() di AWAL fungsi ini — bukan
     *   dari nol. Jadi kalau sebelumnya robot habis belok(), arah
     *   barunya otomatis jadi "lurus" yang baru untuk jalanLurus()
     *   berikutnya. Ini yang bikin urutan langkah() di atas nyambung
     *   tanpa perlu reset manual di antaranya.
     */
    private void jalanLurus(double jarakInci) {

        int targetTick = inciKeTick(jarakInci);
        double arahTarget = getArah();

        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        while (opModeIsActive() && posisiRataRata() < targetTick) {

            int sisaTick = targetTick - posisiRataRata();
            double persenSisa = (double) sisaTick / targetTick;
            double powerDasar = Range.clip(KECEPATAN * persenSisa, POWER_MINIMUM, KECEPATAN);

            double errorArah = bedakanSudut(arahTarget, getArah());
            double koreksi = errorArah * KEKUATAN_KOREKSI_LURUS;

            double powerKiri  = Range.clip(powerDasar - koreksi, -1.0, 1.0);
            double powerKanan = Range.clip(powerDasar + koreksi, -1.0, 1.0);

            motorKiri.setPower(powerKiri);
            motorKanan.setPower(powerKanan);

            telemetry.addData("jalanLurus", "%.1f / %.1f inci", tickKeInci(posisiRataRata()), jarakInci);
            telemetry.addData("Power dasar", "%.3f | Koreksi %.3f", powerDasar, koreksi);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   belok() — Auto02 metode 4, apa adanya
    // ========================================================================
    /** Persis metode4_ImuProporsional di Auto02_Belok. Lihat file itu
     *  untuk penjelasan lengkap kenapa IMU + proporsional dipilih. */
    private void belok(double derajatRelatif) {

        double arahTarget = getArah() + derajatRelatif;

        while (opModeIsActive() && Math.abs(bedakanSudut(arahTarget, getArah())) > TOLERANSI_DERAJAT) {

            double sisa = bedakanSudut(arahTarget, getArah());
            double persenSisa = Math.abs(sisa) / Math.abs(derajatRelatif);
            double arahSekarang = Math.signum(sisa);

            double powerAbs = Range.clip(KECEPATAN_BELOK * persenSisa, POWER_MINIMUM, KECEPATAN_BELOK);
            double powerBertanda = powerAbs * arahSekarang;

            motorKiri.setPower(-powerBertanda);
            motorKanan.setPower(powerBertanda);

            telemetry.addData("belok", "%.1f -> %.1f derajat", getArah(), arahTarget);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   FUNGSI PEMBANTU — sama seperti Auto01 & Auto02
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
 *  PERCOBAAN 1 — Ukur drift
 *    a. Tandai titik awal robot di lantai (isolasi/tape)
 *    b. Jalankan OpMode ini sampai selesai
 *    c. Ukur seberapa jauh posisi akhir dari tanda awal
 *    d. Ini namanya AKUMULASI ERROR — tiap langkah kecil meleset
 *       sedikit, dan meleset-meleset kecil itu menumpuk. Ini alasan
 *       kenapa lomba FTC beneran sering pakai odometri (lihat folder
 *       mechanisms/odometry) untuk tahu posisi PASTI, bukan cuma
 *       "kira-kira sudah jalan segini jauh".
 *
 *  PERCOBAAN 2 — Ganti bentuk
 *    a. Ubah urutan jalanLurus()/belok() supaya bentuknya SEGITIGA
 *       (3 sisi, belok 120 derajat tiap sudut) alih-alih kotak
 *    b. Total belokan pada bentuk tertutup apa pun akhirnya harus
 *       360 derajat. Coba buktikan ini benar untuk segitigamu.
 *
 *  PERCOBAAN 3 — Rusak salah satu fungsi
 *    a. Di jalanLurus(), set KEKUATAN_KOREKSI_LURUS = 0
 *    b. Jalankan lagi bentuk kotaknya. Apa yang berubah?
 *
 *  TANTANGAN
 *    a. Tambah fungsi baru: jalanMundur(inci) — kebalikan jalanLurus()
 *    b. Buat "huruf L" pakai jalanLurus(), belok(), jalanMundur()
 * ============================================================================
 */
