package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

/**
 * ============================================================================
 *   TELEOP DASAR — 3 Cara Memetakan Gamepad ke Motor
 * ============================================================================
 *
 *   Bedanya dari Auto01/02/03: di autonomous, kode yang memutuskan
 *   semuanya sebelum robot bergerak. Di TeleOp, MANUSIA yang
 *   memutuskan, tiap frame, lewat gamepad. Tugas kode di sini cuma
 *   satu: ubah posisi stick jadi power motor, secepat dan sehalus
 *   mungkin, berulang-ulang selama pertandingan.
 *
 *   Karena TeleOp cuma jalan SATU gaya kontrol dalam satu waktu
 *   (nggak seperti Auto yang bisa comment/uncomment baris), caranya
 *   ganti metode di sini beda: ubah MODE_AKTIF di bagian bawah.
 *
 *   Metode 1 gampang dipahami tapi susah dipakai satu tangan.
 *   Metode 3 sedikit lebih rumit tapi paling nyaman dikendarai.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive -> sama seperti Auto01/02/03
 *     gamepad1                -> stick kiri & kanan
 *
 * ============================================================================
 */

@TeleOp(name = "TeleOp Dasar (Belajar)", group = "Belajar")
public class Teleop04_Dasar extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — PILIH MODE DI SINI
    // ========================================================================
    // Cuma satu mode yang aktif tiap saat. Ganti angkanya, upload ulang.

    private static final int MODE_TANK        = 1;
    private static final int MODE_ARCADE       = 2;
    private static final int MODE_ARCADE_EXPO  = 3;

    private static final int MODE_AKTIF = MODE_ARCADE;

    /**
     * PANGKAT KURVA EXPO (dipakai MODE_ARCADE_EXPO)
     *
     * 1.0 = sama persis dengan arcade biasa (linear)
     * 3.0 = kurva kubik — gerakan kecil di tengah stick jadi HALUS
     *       BANGET, gerakan besar di ujung stick tetap dapat power penuh
     *
     * Harus GANJIL (1, 3, 5) supaya tanda plus/minus stick tetap
     * kebawa ke hasilnya. Pangkat genap akan menghilangkan tanda
     * minus dan robot cuma bisa jalan satu arah.
     */
    private static final double PANGKAT_EXPO = 3.0;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor motorKiri;
    private DcMotor motorKanan;

    // ========================================================================
    //   BAGIAN 3 — PROGRAM UTAMA
    // ========================================================================

    @Override
    public void runOpMode() {

        motorKiri  = hardwareMap.get(DcMotor.class, "left_drive");
        motorKanan = hardwareMap.get(DcMotor.class, "right_drive");

        motorKiri.setDirection(DcMotor.Direction.REVERSE);
        motorKanan.setDirection(DcMotor.Direction.FORWARD);

        // FLOAT dipilih di sini, BUKAN BRAKE seperti di Auto01/02/03.
        // Robot jadi meluncur sedikit tiap kali stick dilepas — tapi
        // itu justru lebih nyaman buat pengemudi manusia daripada
        // ngerem mendadak tiap kali stick balik ke tengah. Autonomous
        // butuh BRAKE karena presisi; TeleOp butuh FLOAT karena rasa.
        motorKiri.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motorKanan.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.addData("Mode aktif", namaMode(MODE_AKTIF));
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            switch (MODE_AKTIF) {
                case MODE_TANK:
                    metode1_TankDrive();
                    break;
                case MODE_ARCADE:
                    metode2_ArcadeDrive();
                    break;
                case MODE_ARCADE_EXPO:
                    metode3_ArcadeExpo();
                    break;
            }

            telemetry.update();
        }
    }

    // ========================================================================
    //   METODE 1 — TANK DRIVE
    // ========================================================================
    /**
     * IDENYA:
     *   Stick kiri langsung kendalikan motor kiri. Stick kanan
     *   langsung kendalikan motor kanan. Nggak ada hitung-hitungan
     *   sama sekali — pemetaan paling harfiah yang mungkin.
     *
     * KENAPA NAMANYA "TANK":
     *   Sama seperti tank sungguhan atau alat berat: dua tuas
     *   terpisah, satu per sisi.
     *
     * KENAPA GAMPANG DIPAHAMI:
     *   Push stick kiri ke depan -> roda kiri ke depan. Titik.
     *   Nggak ada "arti tersembunyi" di balik angkanya.
     *
     * KENAPA SUSAH DIKENDARAI:
     *   Buat jalan LURUS, dua stick harus didorong PERSIS sama jauh
     *   dengan DUA jari yang berbeda secara bersamaan. Coba sendiri —
     *   ini lebih susah dari kelihatannya, apalagi sambil grogi di
     *   pertandingan.
     *
     *   gamepad1.left_stick_y itu NEGATIF waktu stick didorong ke
     *   ATAS (ini kebiasaan aneh yang sudah lama ada dari joystick
     *   pesawat). Makanya dikasih tanda minus di depan supaya
     *   "dorong ke atas" = "power positif" = "maju".
     */
    private void metode1_TankDrive() {

        double powerKiri  = -gamepad1.left_stick_y;
        double powerKanan = -gamepad1.right_stick_y;

        motorKiri.setPower(powerKiri);
        motorKanan.setPower(powerKanan);

        telemetry.addData("MODE", "1 — Tank Drive");
        telemetry.addData("Stick kiri", "%.2f", powerKiri);
        telemetry.addData("Stick kanan", "%.2f", powerKanan);
    }

    // ========================================================================
    //   METODE 2 — ARCADE DRIVE
    // ========================================================================
    /**
     * IDENYA:
     *   Satu stick aja. Sumbu Y (depan-belakang) = maju/mundur.
     *   Sumbu X (kiri-kanan) = belok. Digabung matematis:
     *
     *     powerKiri  = maju + belok
     *     powerKanan = maju - belok
     *
     *   Kenapa gitu? Kalau mau maju LURUS (belok = 0), dua motor
     *   dapat power yang sama persis -> jalan lurus otomatis, tanpa
     *   pengemudi harus menyamakan dua stick secara manual.
     *
     *   Kalau mau muter di tempat (maju = 0), satu motor dapat
     *   power positif, satunya negatif -> muter, sama seperti Auto02.
     *
     *   Kalau dua-duanya dipakai bersamaan, robot belok SAMBIL jalan
     *   — kayak nyetir mobil beneran.
     *
     * KENAPA LEBIH ENAK:
     *   Cuma butuh SATU tangan/stick. Tangan yang satunya bebas
     *   buat kontrol mekanisme lain (intake, launcher, dst).
     *
     * MASALAH: CLIPPING
     *   maju + belok bisa saja lebih dari 1.0 atau kurang dari -1.0
     *   (misalnya maju=0.8, belok=0.8 -> 1.6). Motor nggak ngerti
     *   angka di luar -1..1, jadi harus dipotong (clip) dulu.
     */
    private void metode2_ArcadeDrive() {

        double maju  = -gamepad1.left_stick_y;
        double belok = gamepad1.left_stick_x;

        double powerKiri  = Range.clip(maju + belok, -1.0, 1.0);
        double powerKanan = Range.clip(maju - belok, -1.0, 1.0);

        motorKiri.setPower(powerKiri);
        motorKanan.setPower(powerKanan);

        telemetry.addData("MODE", "2 — Arcade Drive");
        telemetry.addData("Maju", "%.2f", maju);
        telemetry.addData("Belok", "%.2f", belok);
    }

    // ========================================================================
    //   METODE 3 — ARCADE + KURVA EXPO
    // ========================================================================
    /**
     * MASALAH DI METODE 2:
     *   Pemetaan stick ke power itu LINEAR — stick 10% = power 10%.
     *   Kedengarannya adil, tapi di dunia nyata power kecil (di
     *   bawah ~15%) sering nggak cukup buat ngelawan gesekan diam
     *   motor. Jadi ada "zona mati" kecil di tengah stick di mana
     *   robot nggak gerak SAMA SEKALI, lalu tiba-tiba nyentak begitu
     *   power-nya cukup besar. Susah buat gerakan halus/presisi,
     *   misalnya waktu mepet-mepetin ke sample atau specimen.
     *
     * SOLUSINYA — KURVA EXPO:
     *   Pangkatkan nilai stick sebelum dipakai:
     *
     *     hasil = stick^PANGKAT_EXPO   (pangkat ganjil, jaga tanda)
     *
     *   Di stick kecil (0.2), hasilnya jadi lebih kecil lagi
     *   (0.2^3 = 0.008) -> gerakan halus banget di dekat tengah.
     *   Di stick penuh (1.0), hasilnya tetap 1.0 -> power maksimal
     *   tetap bisa dicapai kapan saja dibutuhkan.
     *
     *   Bentuk kurvanya kayak huruf S kalau digambar: landai di
     *   tengah, curam di ujung.
     *
     * KENAPA HARUS DICOBA LANGSUNG, BUKAN CUMA DIBACA:
     *   Efeknya susah dibayangkan dari angka doang. Nyalakan metode
     *   ini, coba gerakkan robot pelan-pelan di dekat dinding —
     *   bandingkan rasanya dengan metode 2.
     */
    private void metode3_ArcadeExpo() {

        double maju  = kurvaExpo(-gamepad1.left_stick_y);
        double belok = kurvaExpo(gamepad1.left_stick_x);

        double powerKiri  = Range.clip(maju + belok, -1.0, 1.0);
        double powerKanan = Range.clip(maju - belok, -1.0, 1.0);

        motorKiri.setPower(powerKiri);
        motorKanan.setPower(powerKanan);

        telemetry.addData("MODE", "3 — Arcade + Expo");
        telemetry.addData("Maju (sesudah expo)", "%.3f", maju);
        telemetry.addData("Belok (sesudah expo)", "%.3f", belok);
    }

    /** Kurva expo. Math.pow butuh angka positif, jadi tanda dipisah dulu. */
    private double kurvaExpo(double nilaiStick) {
        return Math.copySign(Math.pow(Math.abs(nilaiStick), PANGKAT_EXPO), nilaiStick);
    }

    private String namaMode(int mode) {
        switch (mode) {
            case MODE_TANK:       return "Tank Drive";
            case MODE_ARCADE:      return "Arcade Drive";
            case MODE_ARCADE_EXPO: return "Arcade + Expo";
            default:                return "???";
        }
    }
}

/*
 * ============================================================================
 *   LATIHAN — kerjakan berurutan
 * ============================================================================
 *
 *  PERCOBAAN 1 — Rasakan tank drive
 *    a. MODE_AKTIF = MODE_TANK
 *    b. Coba jalan LURUS secepat dan sejauh mungkin
 *    c. Seberapa sering robotnya melenceng karena dua jari nggak
 *       kompak?
 *
 *  PERCOBAAN 2 — Bandingkan dengan arcade
 *    a. MODE_AKTIF = MODE_ARCADE
 *    b. Ulangi jalan lurus. Lebih gampang?
 *    c. Coba muter di tempat tanpa maju sama sekali (stick X aja)
 *
 *  PERCOBAAN 3 — Rasakan expo
 *    a. MODE_AKTIF = MODE_ARCADE_EXPO
 *    b. Coba gerakan HALUS deket dinding/objek, stick digerakkan
 *       dikit-dikit aja
 *    c. Bandingkan rasanya dengan MODE_ARCADE biasa
 *
 *  PERCOBAAN 4 — Rusak PANGKAT_EXPO
 *    a. Ganti PANGKAT_EXPO jadi 2.0 (genap). Apa yang terjadi kalau
 *       stick didorong ke arah negatif? Kenapa?
 *    b. Kembalikan ke angka ganjil (1.0, 3.0, atau 5.0), bandingkan
 *       rasanya
 *
 *  TANTANGAN
 *    a. Tambah "mode presisi": kalau gamepad1.right_bumper ditekan,
 *       kalikan semua power dengan 0.3 supaya robot jalan pelan buat
 *       gerakan presisi
 *    b. Tambah deadzone manual: kalau |nilaiStick| < 0.05, anggap 0
 *       (berguna kalau stick gamepadmu "ngedrift" sedikit di tengah)
 * ============================================================================
 */
