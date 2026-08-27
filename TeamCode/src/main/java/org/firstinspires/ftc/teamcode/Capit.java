package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * ============================================================================
 *   CAPIT — Bagian dari pelajaran Auto15_ArsitekturSubsistem
 * ============================================================================
 *
 *   Subsistem KEDUA, sengaja dibikin SEKECIL mungkin supaya polanya
 *   jelas: SATU class = SATU tanggung jawab. Drivetrain.java cuma
 *   urus gerak. Capit.java cuma urus buka-tutup. Nggak ada satu file
 *   pun yang urus DUA hal sekaligus — beda dengan Auto12, di mana
 *   SATU file OpMode raksasa urus motor, servo, sensor jarak, DAN
 *   sensor warna semua sekaligus.
 * ============================================================================
 */
public class Capit {

    private static final double POSISI_TUTUP = 0.0;
    private static final double POSISI_BUKA  = 1.0;

    private final Servo servo;

    public Capit(HardwareMap hardwareMap) {
        servo = hardwareMap.get(Servo.class, "servo_lengan");
    }

    public void buka() {
        servo.setPosition(POSISI_BUKA);
    }

    public void tutup() {
        servo.setPosition(POSISI_TUTUP);
    }
}
