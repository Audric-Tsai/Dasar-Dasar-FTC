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
 *   AUTO BELOK — 4 Cara Menyuruh Robot Muter di Tempat
 * ============================================================================
 *
 *   Sambungan dari Auto01_MajuSimple. Di situ kamu belajar jalan LURUS.
 *   Sekarang: muter di tempat sampai menghadap arah tertentu.
 *
 *   Kedengarannya mirip masalah yang sama (waktu vs encoder vs IMU),
 *   tapi belok punya masalah baru yang jalan lurus nggak punya:
 *   SLIP. Waktu robot muter di tempat, roda nggak menggelinding bersih
 *   — dia juga SERET ke samping. Encoder nggak bisa lihat seretan itu.
 *
 *   Itu sebabnya urutan terbaiknya di sini beda dari Auto01:
 *   metode encoder BUKAN yang paling akurat. IMU-lah jawabannya,
 *   karena IMU mengukur arah hadap ASLI, bukan hitungan putaran roda.
 *
 *   Jangan lompat ke metode 4. Coba metode 2 dulu, lihat sendiri
 *   kenapa hitungan yang matematisnya benar tetap meleset.
 *
 *   ROBOT INI PAKAI:
 *     left_drive   -> sama seperti Auto01
 *     right_drive  -> sama seperti Auto01
 *     imu          -> sama seperti Auto01
 *
 *   LANJUT KE: Auto03_Gabungan — sekarang kamu punya "jalan lurus"
 *   (Auto01) DAN "muter" (file ini), keduanya bisa digabung jadi
 *   satu autonomous beneran yang menyusuri lebih dari satu sisi.
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Belok (Belajar)", group = "Belajar")
public class Auto02_Belok extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    /**
     * Angka-angka motor & roda ini sama persis dengan Auto01_MajuSimple.
     * Kalau kamu sudah kalibrasi angka itu di sana, salin ke sini juga.
     */
    private static final double TICK_PER_PUTARAN   = 560.0;
    private static final double DIAMETER_RODA_INCI = 3.54;

    /**
     * LEBAR TRACK (INCI)
     *
     * Jarak dari titik tengah roda kiri ke titik tengah roda kanan.
     * UKUR PAKAI PENGGARIS, dari tengah roda ke tengah roda, bukan
     * dari sisi luar ke sisi luar.
     *
     * Angka ini cuma dipakai metode 2. Kalau salah, metode 2 akan
     * meleset — dan itu justru bagian dari pelajarannya.
     */
    private static final double LEBAR_TRACK_INCI = 14.0;

    /** Kecepatan muter, 0.0 - 1.0. Mulai pelan supaya gampang diamati. */
    private static final double KECEPATAN_BELOK = 0.3;

    /** Di bawah power ini motor nggak kuat lawan gesekan diam. */
    private static final double POWER_MINIMUM = 0.15;

    /**
     * TOLERANSI (DERAJAT)
     *
     * Robot nggak akan pernah berhenti PERSIS di angka target —
     * selalu ada sedikit lag antara "IMU baca posisi" dan "motor
     * benar-benar berhenti". Toleransi ini kasih jarak aman supaya
     * loop-nya nggak muter selamanya nunggu angka yang nggak
     * mungkin persis kena.
     */
    private static final double TOLERANSI_DERAJAT = 1.0;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor motorKiri;
    private DcMotor motorKanan;
    private IMU     imu;

    private final ElapsedTime waktu = new ElapsedTime();

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
        telemetry.addLine();
        telemetry.addLine("Arah: derajat POSITIF = belok KIRI (berlawanan jarum jam)");
        telemetry.addLine("      derajat NEGATIF = belok KANAN");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_Waktu(1.0, 90);                 // muter ke kiri selama 1 detik
        //metode2_EncoderTerbuka(90);            // coba belok 90 derajat ke kiri
        //metode3_ImuBangBang(90);               // coba belok 90 derajat ke kiri
        //metode4_ImuProporsional(90);           // coba belok 90 derajat ke kiri
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.addData("Arah akhir (menurut IMU)", "%.1f derajat", getArah());
        telemetry.update();
        sleep(5000);
    }

    // ========================================================================
    //   METODE 1 — PAKAI WAKTU
    // ========================================================================
    /**
     * IDENYA:
     *   Nyalakan motor kiri dan kanan berlawanan arah. Tunggu sekian
     *   detik. Matikan. Sama seperti Auto01 metode 1, tapi buat muter.
     *
     * KENAPA LEBIH PARAH DARIPADA METODE 1 DI AUTO01:
     *   Di jalan lurus, setidaknya "detik" masih berhubungan langsung
     *   dengan jarak. Di sini, kamu bahkan nggak tahu 1 detik itu
     *   berapa derajat — itu tergantung KECEPATAN_BELOK, tegangan
     *   baterai, DAN seberapa licin lantai di bawah roda.
     *
     *   Lantai karpet arena FTC vs lantai ubin gym vs lantai yang
     *   habis dipel — tiga-tiganya kasih hasil derajat yang beda,
     *   walau detiknya sama persis.
     *
     * BUKTIKAN SENDIRI:
     *   Jalankan dengan derajatTarget yang sama 3x di tempat yang
     *   sama. Ukur sudut aslinya pakai busur atau tanda di lantai.
     *   Segitiga hasilnya jarang persis sama.
     */
    private void metode1_Waktu(double detik, double derajatTarget) {

        double arah = Math.signum(derajatTarget); // +1 = kiri, -1 = kanan

        waktu.reset();
        motorKiri.setPower(-KECEPATAN_BELOK * arah);
        motorKanan.setPower(KECEPATAN_BELOK * arah);

        while (opModeIsActive() && waktu.seconds() < detik) {
            telemetry.addData("METODE", "1 — Waktu");
            telemetry.addData("Target kira-kira", "%.0f derajat", derajatTarget);
            telemetry.addData("Berjalan", "%.2f / %.2f detik", waktu.seconds(), detik);
            telemetry.addLine();
            telemetry.addLine("Robot nggak tahu sudah berapa derajat.");
            telemetry.addLine("Dia cuma menghitung waktu.");
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   METODE 2 — ENCODER, GEOMETRI TERBUKA (OPEN LOOP)
    // ========================================================================
    /**
     * IDENYA:
     *   Robot muter di tempat = dua roda jalan berlawanan arah,
     *   masing-masing menelusuri LINGKARAN dengan pusat di tengah
     *   robot dan jari-jari = setengah LEBAR_TRACK_INCI.
     *
     *   Panjang busur yang harus ditempuh tiap roda:
     *     busur = jari-jari x sudut (dalam radian)
     *           = (LEBAR_TRACK_INCI / 2) x (derajat x pi / 180)
     *
     *   Ubah busur itu ke tick pakai rumus yang SAMA PERSIS dengan
     *   Auto01 (inciKeTick). Lalu suruh dua motor RUN_TO_POSITION
     *   ke arah berlawanan.
     *
     * KENAPA INI TERLIHAT SEHARUSNYA BENAR:
     *   Matematikanya sama seperti metode 2/3 di Auto01, dan di sana
     *   encoder terbukti akurat untuk jalan lurus. Kenapa nggak untuk
     *   belok?
     *
     * KENAPA SEBENARNYA MELESET — SLIP:
     *   Roda robot dirancang buat MENGGELINDING ke depan, bukan
     *   muter di tempat. Waktu muter, roda nggak cuma berputar —
     *   dia juga TERGESER (skid) ke samping karena karet roda
     *   melawan gesekan lantai.
     *
     *   Encoder cuma menghitung PUTARAN motor. Dia buta terhadap
     *   geseran itu. Jadi encoder bisa saja bilang "sudah sampai
     *   target 90 derajat", padahal robot aslinya baru muter 70
     *   derajat karena sebagian gerakannya "dimakan" slip.
     *
     *   Ini beda fundamental dari Auto01: di situ SEMUA gerakan
     *   roda = gerakan maju robot. Di sini, SEBAGIAN gerakan roda
     *   hilang jadi geseran yang nggak menggerakkan arah hadap robot.
     *
     * BUKTIKAN SENDIRI:
     *   Coba di lantai licin (ubin) vs lantai kesat (karpet arena).
     *   Target derajat dan kode-nya sama persis. Hasilnya beda jauh.
     */
    private void metode2_EncoderTerbuka(double derajatTarget) {

        double arah = Math.signum(derajatTarget);
        double busurInci = (LEBAR_TRACK_INCI / 2.0) * Math.toRadians(Math.abs(derajatTarget));
        int targetTick = inciKeTick(busurInci);

        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        motorKiri.setTargetPosition((int) (-targetTick * arah));
        motorKanan.setTargetPosition((int) (targetTick * arah));

        motorKiri.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorKanan.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        motorKiri.setPower(KECEPATAN_BELOK);
        motorKanan.setPower(KECEPATAN_BELOK);

        while (opModeIsActive() && motorKiri.isBusy() && motorKanan.isBusy()) {
            telemetry.addData("METODE", "2 — Encoder Terbuka");
            telemetry.addData("Target tick", "%d", targetTick);
            telemetry.addData("Arah IMU sekarang", "%.1f derajat", getArah());
            telemetry.addLine();
            telemetry.addLine("Encoder yakin dia sudah sampai.");
            telemetry.addLine("Tapi encoder nggak bisa lihat slip roda.");
            telemetry.addLine("Bandingkan angka IMU di atas dengan target aslimu.");
            telemetry.update();
        }

        berhenti();

        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    // ========================================================================
    //   METODE 3 — IMU, BANG-BANG (FULL POWER SAMPAI MENTOK)
    // ========================================================================
    /**
     * IDENYA:
     *   Daripada percaya encoder, tanya langsung ke IMU: "sekarang
     *   menghadap ke mana?" Selama masih jauh dari target, muter
     *   FULL SPEED. Begitu masuk toleransi, berhenti.
     *
     *   Ini namanya kontrol BANG-BANG — cuma dua keadaan: nyala
     *   penuh atau mati. Nggak ada di tengah-tengah.
     *
     * KENAPA INI SUDAH BENAR SECARA PRINSIP:
     *   IMU mengukur arah hadap ASLI robot, bukan hitungan roda.
     *   Slip nggak masalah lagi — IMU nggak peduli roda geser atau
     *   nggak, dia cuma peduli badan robot sudah menghadap ke mana.
     *   Ini langsung menyelesaikan masalah metode 2.
     *
     * TAPI MASIH ADA MASALAH BARU — MOMENTUM:
     *   Robot muter kencang, lalu tiba-tiba power = 0 tepat di
     *   target. Massa robot yang sedang berputar nggak langsung
     *   berhenti — dia MELUNCUR lewat beberapa derajat dulu sebelum
     *   benar-benar diam. Sama persis seperti masalah "BRAK" di
     *   Auto01 metode 3, tapi sekarang buat rotasi.
     *
     * BUKTIKAN SENDIRI:
     *   Jalankan ke 90 derajat. Lihat angka "Arah akhir" di layar
     *   setelah OpMode selesai. Biasanya lewat beberapa derajat.
     *   Naikkan KECEPATAN_BELOK, lihat overshoot-nya makin parah.
     */
    private void metode3_ImuBangBang(double derajatTarget) {

        double arahTarget = getArah() + derajatTarget;

        while (opModeIsActive() && Math.abs(bedakanSudut(arahTarget, getArah())) > TOLERANSI_DERAJAT) {

            double arah = Math.signum(bedakanSudut(arahTarget, getArah()));

            motorKiri.setPower(-KECEPATAN_BELOK * arah);
            motorKanan.setPower(KECEPATAN_BELOK * arah);

            telemetry.addData("METODE", "3 — IMU Bang-Bang");
            telemetry.addData("Target", "%.1f derajat", arahTarget);
            telemetry.addData("Sekarang", "%.1f derajat", getArah());
            telemetry.addData("Sisa", "%.1f derajat", bedakanSudut(arahTarget, getArah()));
            telemetry.addLine();
            telemetry.addLine("Full power terus sampai mentok toleransi.");
            telemetry.addLine("Perhatikan nanti dia lewat sedikit dari target.");
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   METODE 4 — IMU, PROPORSIONAL
    // ========================================================================
    /**
     * MASALAH DI METODE 3:
     *   Power selalu penuh sampai detik terakhir, jadi momentumnya
     *   juga selalu maksimal waktu dimatikan. Solusinya sama seperti
     *   Auto01 metode 4: kurangi power waktu mendekati target.
     *
     *     power = KECEPATAN_BELOK x (sisa derajat / derajatTarget)
     *
     *   Jauh dari target -> power besar. Dekat target -> power kecil.
     *   Kontrol proporsional lagi — sekarang errornya SUDUT, bukan
     *   JARAK seperti di Auto01, tapi rumusnya persis sama bentuknya.
     *
     * KENAPA INI JADI GABUNGAN TERBAIK:
     *   - Kebal slip, karena tetap pakai IMU (warisan metode 3)
     *   - Nggak overshoot, karena melambat mendekati target (warisan
     *     ide metode 4 di Auto01)
     *
     * INI HURUF P DARI PID — LAGI:
     *   Kalau kamu sudah paham metode 4 di Auto01, ini adalah
     *   penerapan konsep yang SAMA di sumbu yang berbeda (sudut,
     *   bukan jarak). Auto03 nanti akan menggabungkan dua-duanya
     *   sekaligus dalam satu autonomous.
     */
    private void metode4_ImuProporsional(double derajatTarget) {

        double arahTarget = getArah() + derajatTarget;

        while (opModeIsActive() && Math.abs(bedakanSudut(arahTarget, getArah())) > TOLERANSI_DERAJAT) {

            double sisa = bedakanSudut(arahTarget, getArah());

            // Persen dihitung dari NILAI MUTLAK supaya nggak peduli arah.
            // Arah putar dipisah ke variabel sendiri (arahSekarang) — itu
            // juga sebabnya kalau sampai overshoot, arahSekarang otomatis
            // kebalik dan motor jadi mengoreksi balik ke target.
            double persenSisa = Math.abs(sisa) / Math.abs(derajatTarget);
            double arahSekarang = Math.signum(sisa);

            double powerAbs = Range.clip(KECEPATAN_BELOK * persenSisa, POWER_MINIMUM, KECEPATAN_BELOK);
            double powerBertanda = powerAbs * arahSekarang;

            motorKiri.setPower(-powerBertanda);
            motorKanan.setPower(powerBertanda);

            telemetry.addData("METODE", "4 — IMU Proporsional");
            telemetry.addData("Target", "%.1f derajat", arahTarget);
            telemetry.addData("Sekarang", "%.1f derajat", getArah());
            telemetry.addData("Sisa", "%.1f derajat", sisa);
            telemetry.addData("Power sekarang", "%.3f", powerBertanda);
            telemetry.addLine();
            telemetry.addLine("Lihat power-nya turun pelan-pelan mendekati target.");
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   FUNGSI PEMBANTU
    // ========================================================================

    /** Keliling roda dalam INCI. Sama seperti Auto01. */
    private double kelilingRoda() {
        return Math.PI * DIAMETER_RODA_INCI;
    }

    /** Ubah jarak (INCI) menjadi jumlah tick encoder. Sama seperti Auto01. */
    private int inciKeTick(double inci) {
        return (int) ((inci / kelilingRoda()) * TICK_PER_PUTARAN);
    }

    /** Arah hadap robot dalam derajat. Belok kiri = positif. */
    private double getArah() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    /**
     * Selisih dua sudut, dibereskan supaya selalu di antara -180
     * dan 180. Tanpa ini, target 179 lalu posisi -179 akan terbaca
     * beda 358 derajat, padahal aslinya cuma 2 derajat.
     *
     * Ini fungsi yang sama seperti yang "ditulis inline" di Auto01
     * metode 5 — di sini dijadikan fungsi karena dipakai 2x.
     */
    private double bedakanSudut(double target, double sekarang) {
        double selisih = target - sekarang;
        while (selisih >  180) selisih -= 360;
        while (selisih < -180) selisih += 360;
        return selisih;
    }

    /** Matikan kedua motor. */
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
 *  PERCOBAAN 1 — Rasakan slip
 *    a. Jalankan metode 2 target 90 derajat di karpet arena
 *    b. Catat angka "Arah akhir" di layar setelah selesai
 *    c. Ulangi di lantai ubin/licin
 *    d. Selisihnya seberapa besar?
 *
 *  PERCOBAAN 2 — Buktikan IMU kebal slip
 *    a. Ulangi percobaan 1 tapi pakai metode 3
 *    b. Apakah selisih karpet-vs-ubin sekarang jauh lebih kecil?
 *
 *  PERCOBAAN 3 — Lihat overshoot
 *    a. Jalankan metode 3 target 180 derajat, catat arah akhir
 *    b. Jalankan metode 4 dengan target yang sama
 *    c. Mana yang lebih dekat ke 180 persis?
 *
 *  PERCOBAAN 4 — Ukur LEBAR_TRACK_INCI dengan salah sengaja
 *    a. Ganti LEBAR_TRACK_INCI jadi setengah dari angka aslinya
 *    b. Tebak dulu: metode 2 bakal kurang muter atau kelebihan muter?
 *    c. Jalankan, lihat apakah tebakanmu benar
 *    d. Kembalikan angkanya
 *
 *  PERCOBAAN 5 — Dorong robotnya
 *    a. Jalankan metode 4 target 90 derajat
 *    b. Waktu dia lagi muter, tahan sebentar pakai tangan lalu lepas
 *    c. Apa yang terjadi? Kenapa?
 *
 *  TANTANGAN
 *    a. Gabungkan Auto01 metode 4 dan Auto02 metode 4: buat robot
 *       jalan maju 24 inci, lalu belok 90 derajat, lalu jalan maju
 *       lagi 24 inci — semua dalam satu runOpMode(). Ini cikal
 *       bakal Auto03.
 * ============================================================================
 */
