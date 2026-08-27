package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * ============================================================================
 *   DRIVETRAIN — Bagian dari pelajaran Auto15_ArsitekturSubsistem
 * ============================================================================
 *
 *   Baca Auto15_ArsitekturSubsistem.java DULU buat penjelasan lengkap
 *   kenapa file ini ada. Ringkasnya: ini SUBSISTEM DRIVETRAIN, ditulis
 *   SEKALI di sini, dipakai ULANG oleh OpMode mana pun yang butuh —
 *   bukan disalin-tempel dan ditulis ulang di tiap file Auto0N seperti
 *   yang terjadi sepanjang Auto01-14.
 *
 *   Class ini BUKAN OpMode — perhatikan TIDAK ADA @Autonomous/@TeleOp
 *   di atasnya, dan tidak extends LinearOpMode. Dia cuma class Java
 *   BIASA yang MEMEGANG referensi ke dua motor dan tahu cara
 *   menggerakkannya. FTC SDK cuma memindai class yang PUNYA anotasi
 *   @Autonomous/@TeleOp buat dimasukkan ke daftar OpMode di Driver
 *   Station — class polos seperti ini otomatis diabaikan (nggak
 *   akan muncul di daftar itu), yang memang itu yang kita mau.
 * ============================================================================
 */
public class Drivetrain {

    private static final double TICK_PER_PUTARAN   = 560.0;
    private static final double DIAMETER_RODA_INCI = 3.54;
    private static final double KECEPATAN = 0.3;
    private static final double POWER_MINIMUM = 0.15;
    private static final double KECEPATAN_BELOK = 0.3;
    private static final double TOLERANSI_DERAJAT = 1.0;

    private final DcMotor motorKiri;
    private final DcMotor motorKanan;
    private final LinearOpMode opMode; // buat akses opModeIsActive() & telemetry

    /**
     * Konstruktor: di sinilah SEMUA setup hardware (setDirection,
     * setZeroPowerBehavior) yang dulu diulang-ulang di Bagian 3 tiap
     * file Auto0N sekarang cuma ditulis SEKALI, di SATU tempat.
     */
    public Drivetrain(HardwareMap hardwareMap, LinearOpMode opMode) {
        this.opMode = opMode;

        motorKiri  = hardwareMap.get(DcMotor.class, "left_drive");
        motorKanan = hardwareMap.get(DcMotor.class, "right_drive");
        motorKiri.setDirection(DcMotor.Direction.REVERSE);
        motorKanan.setDirection(DcMotor.Direction.FORWARD);
        motorKiri.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorKanan.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    /** Jalan lurus, versi sederhana dari Auto01 metode 4. */
    public void jalanLurus(double jarakInci) {

        int targetTick = inciKeTick(jarakInci);
        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        while (opMode.opModeIsActive() && posisiRataRata() < targetTick) {
            int sisa = targetTick - posisiRataRata();
            double power = Range.clip(KECEPATAN * ((double) sisa / targetTick), POWER_MINIMUM, KECEPATAN);
            motorKiri.setPower(power);
            motorKanan.setPower(power);
            opMode.telemetry.addData("Drivetrain.jalanLurus", "%.1f / %.1f inci",
                    tickKeInci(posisiRataRata()), jarakInci);
            opMode.telemetry.update();
        }
        berhenti();
    }

    /** Belok di tempat, versi sederhana berbasis waktu (lihat Auto02/09 buat versi IMU/PID). */
    public void belokWaktu(double powerKiri, double powerKanan, double detik) {
        ElapsedTime waktu = new ElapsedTime();
        while (opMode.opModeIsActive() && waktu.seconds() < detik) {
            motorKiri.setPower(powerKiri);
            motorKanan.setPower(powerKanan);
        }
        berhenti();
    }

    public void berhenti() {
        motorKiri.setPower(0);
        motorKanan.setPower(0);
    }

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
        return (Math.abs(motorKiri.getCurrentPosition()) + Math.abs(motorKanan.getCurrentPosition())) / 2;
    }
}
