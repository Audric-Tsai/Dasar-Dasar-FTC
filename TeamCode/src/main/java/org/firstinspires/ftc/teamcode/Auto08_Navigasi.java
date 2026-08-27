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
 *   AUTO NAVIGASI — Menggabungkan SEMUA Pelajaran Jadi "Pergi ke (X, Y)"
 * ============================================================================
 *
 *   Ini pelajaran paling "besar" di seri Auto0N sejauh ini. Nggak ada
 *   konsep baru yang aneh — semuanya daur ulang dari file-file
 *   sebelumnya. Yang baru adalah CARA MENGGABUNGKANNYA:
 *
 *     Auto01 -> jalan lurus, pentingnya encoder
 *     Auto02 -> muter, pentingnya IMU, atan2 buat sudut (di sini)
 *     Auto04 -> PID: P + I + D
 *     Auto05 -> belokPID(), pola PID dipakai buat gerakan nyata
 *     Auto07 -> odometri: robot tahu (X, Y, heading) sendiri, TERUS-
 *               MENERUS, bukan cuma "sudah jalan berapa jauh"
 *
 *   Auto03 dan Auto05 gerak dengan PERINTAH RELATIF: "maju 24 inci",
 *   "belok 90 derajat". Robot nggak pernah tahu dia lagi di mana di
 *   lapangan — dia cuma tahu APA YANG HARUS DILAKUKAN SELANJUTNYA,
 *   dalam urutan tetap yang KAMU tulis manual.
 *
 *   File ini beda secara fundamental: kamu kasih tahu robot KE MANA
 *   (koordinat X, Y di lapangan), dan robot yang MENGHITUNG SENDIRI
 *   berapa jauh dan ke arah mana dia harus gerak, TIAP SAAT, dari
 *   posisi dia SEKARANG (yang terus di-update pakai Auto07). Ini
 *   namanya "go-to-point" atau "field-centric autonomous".
 *
 * ----------------------------------------------------------------------------
 *   SISTEM KOORDINAT — WAJIB DIPAHAMI SEBELUM BACA KODE DI BAWAH
 * ----------------------------------------------------------------------------
 *
 *   Titik (0, 0) = posisi robot waktu OpMode di-PLAY.
 *   Sumbu +X      = arah hadap robot waktu di-PLAY (imu.resetYaw()).
 *   Sumbu +Y      = 90 derajat ke KIRI dari situ.
 *   Heading 0 derajat   = menghadap +X.
 *   Heading positif     = berputar ke KIRI (CCW), menuju +Y.
 *
 *   Gambar dari atas (seperti peta), robot mulai di titik O menghadap
 *   ke kanan gambar:
 *
 *          +Y
 *           ^
 *           |
 *           |     . (24, 24)  <- salah satu target contoh di bawah
 *           |
 *           |
 *     O-----+----------------> +X      (robot mulai di sini, heading 0)
 *    (0,0)
 *
 *   Ini PERSIS konvensi matematika sudut biasa (lingkaran trigonometri
 *   standar) — sengaja disamakan supaya rumus atan2/sin/cos di bawah
 *   nggak perlu ubah tanda macam-macam. Ini juga konvensi yang SAMA
 *   dipakai buat rumus deltaX/deltaY di Auto07_Odometri.
 *
 * ----------------------------------------------------------------------------
 *   MATEMATIKA INTI — DUA RUMUS YANG DIPAKAI DI SETIAP METODE
 * ----------------------------------------------------------------------------
 *
 *   1) JARAK KE TARGET (teorema Pythagoras):
 *
 *        deltaX = targetX - x
 *        deltaY = targetY - y
 *        jarak  = akar(deltaX^2 + deltaY^2)     <- Math.hypot(deltaY, deltaX)
 *
 *      CONTOH ANGKA: robot di (0,0), target (24, 24).
 *        deltaX = 24, deltaY = 24
 *        jarak  = akar(24^2 + 24^2) = akar(1152) = 33.9 inci
 *
 *   2) ARAH MENUJU TARGET (atan2, BUKAN atan biasa):
 *
 *        arahMenujuTarget = atan2(deltaY, deltaX)
 *
 *      KENAPA HARUS atan2, BUKAN Math.atan(deltaY / deltaX)?
 *
 *      atan() biasa cuma ngasih jawaban antara -90 dan +90 derajat —
 *      dia BUTA arah kuadran, karena deltaY/deltaX kehilangan info
 *      tanda begitu dibagi (negatif/negatif = positif, sama kayak
 *      positif/positif). Contoh nyata:
 *
 *        Target di KIRI-DEPAN:  deltaX=+5,  deltaY=+5
 *          atan(5/5)  = atan(1)  = 45 derajat   -> BENAR
 *
 *        Target di KIRI-BELAKANG: deltaX=-5, deltaY=+5
 *          atan(5/-5) = atan(-1) = -45 derajat  -> SALAH!
 *          Aslinya target itu di 135 derajat (belakang-kiri), tapi
 *          atan() biasa nunjuk ke -45 derajat (depan-kanan) — arah
 *          yang PERSIS BERLAWANAN sama sekali salah kuadran.
 *
 *      atan2(deltaY, deltaX) ngasih dua angka TERPISAH (bukan hasil
 *      bagi), jadi dia tahu persis di kuadran mana targetnya, dan
 *      selalu benar di seluruh lingkaran 360 derajat (-180 sampai
 *      180). Kalau kamu pernah pakai atan() biasa buat navigasi dan
 *      robotnya kadang muter ke arah yang aneh 180 derajat kebalik —
 *      ini penyebabnya.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive         -> drivetrain, sama Auto01-05
 *     encoder_maju, encoder_geser     -> dead wheel pod, sama Auto07
 *     imu                             -> sama semua file sebelumnya
 *
 *   Jangan lompat ke metode 3. Coba metode 1 dulu, sengaja bikin
 *   putarannya meleset dikit, dan lihat sendiri kenapa dia bisa
 *   nggak tepat sampai ke titik yang dimaksud.
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Navigasi (Belajar)", group = "Belajar")
public class Auto08_Navigasi extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    // --- Drivetrain (sama seperti Auto01-05) -------------------------------
    private static final double TICK_PER_PUTARAN_RODA   = 560.0;
    private static final double DIAMETER_RODA_INCI       = 3.54;

    // --- Dead wheel pod odometri (sama seperti Auto07) ----------------------
    private static final double TICK_PER_PUTARAN_ENCODER = 8192.0; // REV Through Bore Encoder V2
    private static final double DIAMETER_RODA_ENCODER_INCI = 1.378; // ~35mm, UKUR SENDIRI
    private static final double OFFSET_GESER_INCI = 6.0;            // UKUR/kalibrasi sendiri, lihat Auto07

    // --- Batas power & toleransi --------------------------------------------
    private static final double KECEPATAN_MAKS = 0.5;
    private static final double TOLERANSI_JARAK_INCI = 1.0;
    private static final double TOLERANSI_DERAJAT = 1.0;
    private static final double WAKTU_MAKS_DETIK = 6.0;

    // --- PID jarak (dipakai metode 1 & metode 2/3) --------------------------
    private static final double KP_JARAK = 0.05;
    private static final double KI_JARAK = 0.0008;
    private static final double KD_JARAK = 0.01;
    private static final double INTEGRAL_MAKS_JARAK = 10.0;

    // --- PID arah, buat MUTER DI TEMPAT (belokPID, metode 1) ----------------
    private static final double KP_ARAH = 0.0060;
    private static final double KI_ARAH = 0.00003;
    private static final double KD_ARAH = 0.0009;
    private static final double INTEGRAL_MAKS_ARAH = 40.0;

    // --- P heading-hold sederhana, buat jalan lurus (metode 1) --------------
    private static final double KEKUATAN_KOREKSI_LURUS = 0.03;

    /**
     * KENAPA ADA KP_KEJAR/KD_KEJAR TERPISAH DARI KP_ARAH/KD_ARAH:
     *
     * KP_ARAH di atas di-tuning buat MUTER DI TEMPAT — power dari 0
     * sampai KECEPATAN_MAKS, motor kiri-kanan berlawanan arah penuh.
     *
     * Di metode 2/3, koreksi arah ini bukan satu-satunya sumber
     * power motor — dia DITAMBAHKAN ke powerDasar yang juga sedang
     * mendorong robot maju. Menyetir sambil jalan itu secara fisik
     * beda geometrinya dari muter di tempat. Gain yang pas buat satu
     * kasus belum tentu pas buat kasus lain — makanya dipisah, bukan
     * asal pakai ulang angka yang sama cuma karena "sama-sama
     * ngoreksi sudut".
     */
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

    // ========================================================================
    //   BAGIAN 2b — POSISI ROBOT (POSE) — STATE YANG HIDUP SEPANJANG OPMODE
    // ========================================================================
    /**
     * Di Auto07, x/y cuma variabel LOKAL di dalam satu metode — begitu
     * metode-nya selesai, posisinya "lupa". Di sini x/y dijadikan
     * FIELD (variabel milik class), supaya SEMUA metode dan SEMUA
     * panggilan pergiKeTitik() berturut-turut berbagi satu posisi
     * yang sama dan terus nyambung — persis seperti odometri
     * sungguhan berjalan sepanjang satu match penuh, bukan di-reset
     * tiap kali ganti gerakan.
     */
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

        // Titik nol pose HARUS diambil SETELAH imu.resetYaw(), supaya
        // headingSebelumnyaRadian mulai dari 0 juga — kalau nggak,
        // sumbu +X yang dijelaskan di banner atas nggak akan cocok
        // dengan arah hadap robot yang sebenarnya waktu PLAY ditekan.
        headingSebelumnyaRadian = getArahRadian();

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.addLine("Robot akan pergi ke titik (X, Y) target secara mandiri.");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_BelokLaluJalan(24, 24);
        //metode2_KejarTitik(24, 24);
        //metode3_Lintasan();
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.addData("Posisi akhir", "X=%.2f  Y=%.2f", x, y);
        telemetry.addData("Target", "X=24.0  Y=24.0");
        telemetry.update();
        sleep(5000);
    }

    // ========================================================================
    //   METODE 1 — BELOK DULU, BARU JALAN (DUA FASE TERPISAH)
    // ========================================================================
    /**
     * LANGKAH-LANGKAHNYA:
     *
     *   1. Perbarui pose, hitung deltaX/deltaY/arahMenujuTarget pakai
     *      atan2 (rumusnya persis seperti di banner atas file ini).
     *   2. belokPID() SEKALI ke arah itu (fungsi yang sama persis
     *      dari Auto05_GabunganPID).
     *   3. KUNCI arah hasil belokan itu ke satu variabel
     *      (arahDikunci), lalu jalan lurus PID sambil menjaga arah
     *      itu TETAP (KEKUATAN_KOREKSI_LURUS yang sama dari
     *      Auto03/Auto05), berhenti waktu jarak SISA ke target
     *      (dihitung ulang dari pose LIVE tiap loop) masuk toleransi.
     *
     * KENAPA INI TERASA SEPERTI LANGKAH YANG BENAR:
     *   Ini persis cara MANUSIA kasih arahan: "belok ke sana, terus
     *   jalan lurus." Masuk akal, dan untuk jarak pendek biasanya
     *   HASILNYA CUKUP BAGUS.
     *
     * CACATNYA — ARAH DIKUNCI DI AWAL, NGGAK PERNAH DIKOREKSI LAGI:
     *   belokPID() berhenti begitu error masuk TOLERANSI_DERAJAT
     *   (1.0 derajat) — bukan PERSIS 0. Sisa error kecil itu (sampai
     *   1 derajat) langsung DIKUNCI jadi arahDikunci dan dipakai
     *   SEPANJANG perjalanan lurus. Robot nggak pernah bertanya lagi
     *   "apa aku masih ngarah ke target yang BENER?" — dia cuma
     *   menjaga arah AWAL itu tetap lurus, walau arah awal itu sendiri
     *   sudah sedikit meleset dari target sebenarnya.
     *
     *   Ingat pelajaran Auto02: "melenceng 3 derajat kelihatan
     *   sepele, tapi setelah 3 meter jadi 15 cm." Prinsip yang sama
     *   berlaku di sini. Makin jauh targetnya, makin besar akibat
     *   dari 1 derajat sisa error yang dikunci di awal ini.
     */
    private void metode1_BelokLaluJalan(double targetX, double targetY) {

        perbaruiPosisi();

        double deltaX = targetX - x;
        double deltaY = targetY - y;
        double arahMenujuTarget = Math.toDegrees(Math.atan2(deltaY, deltaX));
        double putaranDibutuhkan = bedakanSudut(arahMenujuTarget, getArahDerajat());

        telemetry.addData("METODE", "1 — Belok Lalu Jalan");
        telemetry.addData("Posisi sekarang", "X=%.2f  Y=%.2f", x, y);
        telemetry.addData("Arah menuju target (atan2)", "%.1f derajat", arahMenujuTarget);
        telemetry.addData("Perlu belok", "%.1f derajat", putaranDibutuhkan);
        telemetry.update();

        belokPID(putaranDibutuhkan);

        perbaruiPosisi(); // serap sisa gerakan (harusnya kecil) dari fase belok
        double arahDikunci = getArahDerajat();

        majuSampaiTarget(targetX, targetY, arahDikunci);
    }

    /**
     * Jalan lurus, arah DIKUNCI ke satu angka (arahDikunci) yang
     * TIDAK berubah sepanjang fungsi ini berjalan — beda dari
     * pergiKeTitik() di metode 2, yang menghitung ulang arah tiap
     * loop. Berhenti berdasarkan JARAK KE TARGET dari pose live,
     * bukan dari tick encoder drivetrain (beda dari jalanLurusPID
     * Auto05, yang berhenti berdasarkan tick).
     */
    private void majuSampaiTarget(double targetX, double targetY, double arahDikunci) {

        double integralJarak = 0;
        double errorJarakSebelumnya = Math.hypot(targetX - x, targetY - y);

        ElapsedTime waktuTotal = new ElapsedTime();
        ElapsedTime waktuLoop  = new ElapsedTime();

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            perbaruiPosisi();

            double jarakSisa = Math.hypot(targetX - x, targetY - y);
            if (jarakSisa <= TOLERANSI_JARAK_INCI) break;

            double dt = Math.max(waktuLoop.seconds(), 0.001);
            waktuLoop.reset();

            integralJarak = Range.clip(integralJarak + jarakSisa * dt, -INTEGRAL_MAKS_JARAK, INTEGRAL_MAKS_JARAK);
            double turunanJarak = (jarakSisa - errorJarakSebelumnya) / dt;
            errorJarakSebelumnya = jarakSisa;

            double powerDasar = Range.clip(
                    (KP_JARAK * jarakSisa) + (KI_JARAK * integralJarak) + (KD_JARAK * turunanJarak),
                    0, KECEPATAN_MAKS);

            // Arah TIDAK dihitung ulang dari target di sini — cuma
            // dijaga tetap sama seperti arahDikunci. Ini titik lemah
            // yang dijelaskan di javadoc metode1 di atas.
            double errorArah = bedakanSudut(arahDikunci, getArahDerajat());
            double koreksi = errorArah * KEKUATAN_KOREKSI_LURUS;

            double powerKiri  = Range.clip(powerDasar - koreksi, -1.0, 1.0);
            double powerKanan = Range.clip(powerDasar + koreksi, -1.0, 1.0);

            motorKiri.setPower(powerKiri);
            motorKanan.setPower(powerKanan);

            telemetry.addData("majuSampaiTarget", "X=%.2f  Y=%.2f", x, y);
            telemetry.addData("Jarak sisa", "%.2f inci", jarakSisa);
            telemetry.addData("Arah dikunci", "%.1f derajat", arahDikunci);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   METODE 2 — KEJAR TITIK (DIHITUNG ULANG TIAP LOOP)
    // ========================================================================
    /**
     * MASALAH DI METODE 1:
     *   Arah cuma dihitung SEKALI (sebelum mulai jalan), lalu
     *   dikunci. Segala sesuatu yang bikin arah aslinya meleset dari
     *   target — sisa toleransi belokPID, dorongan kecil dari robot
     *   lain, ban yang licin sebelah — nggak pernah dikoreksi lagi
     *   selama jalan lurus.
     *
     * IDE METODE 2:
     *   HAPUS fase "belok dulu" sama sekali. Setiap satu putaran
     *   loop, robot bertanya ULANG: "dari posisiku SEKARANG, ke arah
     *   mana target itu?" — pakai rumus atan2 yang SAMA seperti di
     *   metode 1, tapi dipanggil terus-menerus, bukan sekali di awal.
     *
     *   Robot MENYETIR sambil MAJU secara bersamaan — persis seperti
     *   orang mengendarai mobil menuju suatu titik: nggak berhenti
     *   dulu buat pas-in arah, tapi terus mengoreksi setir kecil-
     *   kecil sepanjang jalan sambil tetap gas.
     *
     *   powerDasar (seberapa cepat maju) dan koreksi (seberapa
     *   banyak menyetir) DIHITUNG TERPISAH tiap loop, persis seperti
     *   jalanLurusPID di Auto05 — bedanya di sini KEDUANYA berubah
     *   terus tiap loop mengikuti pose yang terus di-update, bukan
     *   cuma salah satunya.
     *
     * KENAPA INI LEBIH KEBAL DARIPADA METODE 1:
     *   Kalau ada apa pun yang bikin robot melenceng di tengah
     *   jalan (didorong, slip, dll), loop berikutnya LANGSUNG
     *   menghitung arah baru yang benar menuju target dan mengoreksi
     *   — nggak pernah "terjebak" pada arah lama yang salah kayak
     *   metode 1.
     */
    private void metode2_KejarTitik(double targetX, double targetY) {
        pergiKeTitik(targetX, targetY);
    }

    /**
     * Fungsi inti "go-to-point". Dipanggil langsung oleh metode 2
     * (satu titik), dan dipanggil BERKALI-KALI oleh metode 3 (banyak
     * titik berturut-turut) — sama seperti belokPID() dipakai ulang
     * dari Auto04 ke Auto05, pola yang sama dipakai lagi di sini.
     */
    private void pergiKeTitik(double targetX, double targetY) {

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
            if (jarakSisa <= TOLERANSI_JARAK_INCI) break;

            // INI BEDANYA DARI METODE 1: arah dihitung ULANG di sini,
            // di DALAM loop, pakai posisi yang paling baru.
            double arahMenujuTarget = Math.toDegrees(Math.atan2(deltaY, deltaX));
            double errorArah = bedakanSudut(arahMenujuTarget, getArahDerajat());

            double dt = Math.max(waktuLoop.seconds(), 0.001);
            waktuLoop.reset();

            // --- PID buat KECEPATAN MAJU (berdasarkan jarak sisa) ---
            integralJarak = Range.clip(integralJarak + jarakSisa * dt, -INTEGRAL_MAKS_JARAK, INTEGRAL_MAKS_JARAK);
            double turunanJarak = (jarakSisa - errorJarakSebelumnya) / dt;
            errorJarakSebelumnya = jarakSisa;

            double powerDasar = Range.clip(
                    (KP_JARAK * jarakSisa) + (KI_JARAK * integralJarak) + (KD_JARAK * turunanJarak),
                    0, KECEPATAN_MAKS);

            // --- PD buat SETIR (berdasarkan arah menuju target SEKARANG) ---
            double turunanArah = (errorArah - errorArahSebelumnya) / dt;
            errorArahSebelumnya = errorArah;

            double koreksi = Range.clip(
                    (KP_KEJAR * errorArah) + (KD_KEJAR * turunanArah),
                    -KECEPATAN_MAKS, KECEPATAN_MAKS);

            double powerKiri  = Range.clip(powerDasar - koreksi, -1.0, 1.0);
            double powerKanan = Range.clip(powerDasar + koreksi, -1.0, 1.0);

            motorKiri.setPower(powerKiri);
            motorKanan.setPower(powerKanan);

            telemetry.addData("pergiKeTitik", "menuju (%.1f, %.1f)", targetX, targetY);
            telemetry.addData("Posisi sekarang", "X=%.2f  Y=%.2f", x, y);
            telemetry.addData("Jarak sisa", "%.2f inci", jarakSisa);
            telemetry.addData("Arah menuju target (dihitung ulang)", "%.1f derajat", arahMenujuTarget);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   METODE 3 — LINTASAN (BANYAK TITIK BERTURUT-TURUT)
    // ========================================================================
    /**
     * Bentuk kotak yang SAMA seperti Auto03/Auto05, tapi ditulis
     * dengan cara yang SAMA SEKALI BEDA: bukan daftar perintah
     * RELATIF ("maju 24, belok 90, maju 24, ..."), tapi daftar
     * KOORDINAT ABSOLUT di lapangan.
     *
     *     Auto03/05:  maju(24); belok(90); maju(24); belok(90); ...
     *     Auto08:     pergiKeTitik(24, 0); pergiKeTitik(24, 24);
     *                 pergiKeTitik(0, 24); pergiKeTitik(0, 0);
     *
     * KENAPA INI LEBIH KUAT, BUKAN CUMA BEDA GAYA NULIS:
     *   Kalau salah satu titik meleset di tengah jalan (didorong,
     *   dll), Auto03/05 TETAP melanjutkan perintah RELATIF berikutnya
     *   dari posisi yang salah itu — errornya menumpuk terus
     *   (ingat "akumulasi error" di LATIHAN Auto03).
     *
     *   Di sini, tiap pergiKeTitik() berikutnya dihitung dari POSISI
     *   ASLI robot SEKARANG (bukan "posisi seharusnya"), jadi dia
     *   otomatis mengoreksi diri ke titik yang BENAR berikutnya,
     *   nggak peduli seberapa jauh dia meleset di titik sebelumnya.
     *
     *   Ini juga kenapa banyak tim FTC kompetitif akhirnya pindah ke
     *   gaya "daftar koordinat" kayak gini — koordinatnya bisa
     *   dihitung otomatis dari sensor (misalnya AprilTag, pelajaran
     *   nanti), bukan cuma angka tetap yang kamu ketik manual.
     */
    private void metode3_Lintasan() {

        langkah("1/4 — (24, 0)");   pergiKeTitik(24, 0);
        langkah("2/4 — (24, 24)");  pergiKeTitik(24, 24);
        langkah("3/4 — (0, 24)");   pergiKeTitik(0, 24);
        langkah("4/4 — (0, 0)");    pergiKeTitik(0, 0);
    }

    private void langkah(String nama) {
        telemetry.addData("LANGKAH", nama);
        telemetry.update();
    }

    // ========================================================================
    //   belokPID() — Auto04/Auto05, apa adanya (dipakai metode 1 saja)
    // ========================================================================
    private void belokPID(double derajatRelatif) {

        double arahTarget = getArahDerajat() + derajatRelatif;
        double integralArah = 0;
        double errorArahSebelumnya = bedakanSudut(arahTarget, getArahDerajat());

        ElapsedTime waktuTotal = new ElapsedTime();
        ElapsedTime waktuLoop  = new ElapsedTime();

        while (opModeIsActive() && waktuTotal.seconds() < WAKTU_MAKS_DETIK) {

            double errorArah = bedakanSudut(arahTarget, getArahDerajat());
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

            telemetry.addData("belokPID", "%.1f -> %.1f derajat", getArahDerajat(), arahTarget);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   perbaruiPosisi() — Auto07 metode 4, dipindah jadi update FIELD
    // ========================================================================
    /**
     * Persis algoritma metode4_LacakLengkap() di Auto07_Odometri:
     * baca delta tick dua pod sejak panggilan terakhir, koreksi
     * bacaan pod geser dari busur palsu akibat rotasi (OFFSET_GESER_
     * INCI), putar delta lokal (maju, geser) ke bingkai global pakai
     * heading RATA-RATA selama interval ini, lalu tambahkan ke x/y.
     *
     * Bedanya dari Auto07: di sana ini kode di dalam satu while-loop
     * besar. Di sini dipisah jadi fungsi sendiri supaya bisa
     * dipanggil dari BANYAK tempat berbeda (di dalam loop
     * majuSampaiTarget, di dalam loop pergiKeTitik, dan sesaat
     * sebelum/sesudah belokPID) — semuanya berbagi x/y yang sama.
     */
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

        double deltaX = deltaMaju * Math.cos(headingRataRata) - deltaGeser * Math.sin(headingRataRata);
        double deltaY = deltaMaju * Math.sin(headingRataRata) + deltaGeser * Math.cos(headingRataRata);
        x += deltaX;
        y += deltaY;
    }

    // ========================================================================
    //   FUNGSI PEMBANTU
    // ========================================================================

    private double kelilingRoda() {
        return Math.PI * DIAMETER_RODA_INCI;
    }

    private double tickKeInciRoda(int tick) {
        return (tick / TICK_PER_PUTARAN_RODA) * kelilingRoda();
    }

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

    /** Selisih dua sudut DERAJAT, dibereskan ke antara -180 dan 180. */
    private double bedakanSudut(double target, double sekarang) {
        double selisih = target - sekarang;
        while (selisih >  180) selisih -= 360;
        while (selisih < -180) selisih += 360;
        return selisih;
    }

    /** Selisih dua sudut RADIAN, dibereskan ke antara -pi dan pi. */
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
 *  PERCOBAAN 1 — Verifikasi sistem koordinat
 *    a. Jalankan metode 1 dengan target (24, 0) — cuma sumbu X
 *    b. Robot seharusnya JALAN LURUS tanpa belok sama sekali (karena
 *       arahMenujuTarget = atan2(0, 24) = 0 derajat, sama dengan
 *       heading awal robot)
 *    c. Ganti target ke (0, 24) — cuma sumbu Y. Robot seharusnya
 *       BELOK 90 derajat DULU (ke kiri), baru jalan
 *
 *  PERCOBAAN 2 — Rasakan cacat metode 1
 *    a. Jalankan metode 1 ke target (48, 48) — jarak lebih jauh dari
 *       contoh (24,24) supaya efeknya lebih kelihatan
 *    b. Catat "Posisi akhir" di telemetry setelah selesai. Seberapa
 *       jauh dari (48, 48) yang sebenarnya?
 *    c. Baca ulang javadoc metode1_BelokLaluJalan — itu penyebabnya
 *
 *  PERCOBAAN 3 — Buktikan metode 2 lebih akurat
 *    a. Jalankan metode 2 ke target yang SAMA (48, 48)
 *    b. Bandingkan "Posisi akhir" dengan hasil metode 1. Lebih dekat?
 *
 *  PERCOBAAN 4 — Ganggu robotnya di tengah jalan
 *    a. Jalankan metode 2 ke target yang jauh (misal 60, 0)
 *    b. Waktu robot lagi jalan, geser dia dikit pakai tangan
 *       (jangan kasar)
 *    c. Lihat dia mengoreksi arah sendiri buat tetap menuju target?
 *    d. Coba ulangi dengan metode 1 — apa dia mengoreksi juga, atau
 *       tetap jalan lurus ke arah lama yang sudah salah?
 *
 *  PERCOBAAN 5 — Lintasan penuh
 *    a. Jalankan metode 3, bandingkan bentuk & akurasi kotaknya
 *       dengan Auto03_Gabungan dan Auto05_GabunganPID
 *
 *  TANTANGAN
 *    a. Tambah waypoint ke-5 di metode3_Lintasan() yang membawa
 *       robot ke tengah kotak, (12, 12), SETELAH sampai balik ke
 *       (0,0) — lintasan yang nggak bisa dibuat cuma pakai "maju/
 *       belok" tanpa hitung ulang trigonometri secara manual
 *    b. KP_KEJAR/KD_KEJAR di atas ditebak, bukan diukur. Coba
 *       tuning sendiri pakai urutan yang sama seperti Auto04: naikkan
 *       KP_KEJAR sampai robotnya mulai "goyang" jalurnya menuju
 *       target, lalu turunkan sedikit dan tambah KD_KEJAR sampai
 *       stabil.
 * ============================================================================
 */
