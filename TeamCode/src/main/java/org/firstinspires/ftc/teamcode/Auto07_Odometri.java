package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * ============================================================================
 *   AUTO ODOMETRI — Melacak Posisi (X, Y) Pakai 2 REV Through Bore Encoder
 * ============================================================================
 *
 *   Lihat mechanisms/odometry/01_dead_wheel.md buat penjelasan konsep
 *   dead wheel secara umum. File ini adalah VERSI KODE-nya, dengan
 *   konfigurasi paling umum dipakai kalau kamu cuma punya 2 encoder:
 *
 *     1 pod MAJU (parallel)     -> ngukur gerak depan-belakang
 *     1 pod GESER (perpendicular) -> ngukur gerak kiri-kanan
 *     IMU                        -> ngukur ARAH HADAP (bukan encoder ke-3)
 *
 *   Kenapa nggak pakai 2 pod maju (kiri+kanan) buat hitung heading
 *   sendiri (kayak drivetrain biasa)? Karena IMU JAUH lebih akurat
 *   buat itu, dan kamu sudah punya IMU gratis di Control Hub. Nggak
 *   perlu "membeli ulang" kemampuan yang sudah ada.
 *
 *   TENTANG REV THROUGH BORE ENCODER V2:
 *     - 8192 hitungan (COUNT) per satu putaran penuh poros
 *       (2048 pulsa asli x 4, karena dibaca quadrature)
 *     - Dia BUKAN motor. Dia cuma sensor. Tapi Control/Expansion Hub
 *       cuma bisa MEMBACA sinyal encoder lewat PORT MOTOR — jadi
 *       kabelnya dicolok ke port motor yang TIDAK diisi motor asli,
 *       dan di kode kita tetap panggil hardwareMap.get(DcMotor.class,
 *       ...) buat objeknya, walau kita TIDAK PERNAH memanggil
 *       setPower() ke objek itu. Kita cuma "numpang" port-nya buat
 *       baca getCurrentPosition().
 *
 *   CARA TES PALING GAMPANG — DORONG PAKAI TANGAN:
 *     Dead wheel itu roda BEBAS/PASIF. Kamu nggak perlu nyalain motor
 *     drivetrain sama sekali buat nyoba file ini. Tekan PLAY, lalu
 *     DORONG robotnya pakai tangan ke mana-mana di lantai, sambil
 *     lihat angka X/Y di telemetry berubah.
 *
 *   Jangan lompat ke metode 4. Coba metode 2 dulu, dorong robotnya
 *   sambil diputar, dan lihat sendiri kenapa itu meleset.
 *
 *   LANJUT KE: Auto08_Navigasi — begitu robot tahu (X, Y, heading)
 *   sendiri secara terus-menerus, dia bisa dikasih KOORDINAT TARGET
 *   langsung, bukan cuma perintah relatif "maju sekian, belok sekian".
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Odometri (Belajar)", group = "Belajar")
public class Auto07_Odometri extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    /** REV Through Bore Encoder V2: 8192 count per satu putaran poros. */
    private static final double TICK_PER_PUTARAN_ENCODER = 8192.0;

    /**
     * DIAMETER RODA DEAD WHEEL (INCI)
     *
     * BUKAN roda drivetrain — ini roda kecil di pod odometri (umumnya
     * sekitar 35mm buat pod goBILDA/REV). UKUR PAKAI PENGGARIS punya
     * robotmu sendiri, jangan asal salin dari internet.
     */
    private static final double DIAMETER_RODA_ENCODER_INCI = 1.378; // ~35mm

    /**
     * OFFSET POD GESER (INCI)
     *
     * Jarak dari TITIK PUSAT ROTASI robot (kira-kira tengah robot)
     * ke pod GESER (perpendicular). Dipakai metode 4 buat mengoreksi
     * gerakan palsu yang terbaca pod geser waktu robot MUTER DI
     * TEMPAT (bukan geser beneran).
     *
     * UKUR SENDIRI. PERCOBAAN TANTANGAN di bawah kasih cara
     * mengkalibrasi angka ini pakai eksperimen, bukan cuma penggaris.
     */
    private static final double OFFSET_GESER_INCI = 6.0;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor encoderMaju;   // pod parallel — dicolok ke port motor kosong
    private DcMotor encoderGeser;  // pod perpendicular — dicolok ke port motor kosong
    private IMU imu;

    // ========================================================================
    //   BAGIAN 3 — PROGRAM UTAMA
    // ========================================================================

    @Override
    public void runOpMode() {

        encoderMaju  = hardwareMap.get(DcMotor.class, "encoder_maju");
        encoderGeser = hardwareMap.get(DcMotor.class, "encoder_geser");

        // Encoder ini nggak pernah di-setPower() — bukan motor sungguhan.
        // setDirection() di sini cuma soal ARAH HITUNGAN naik/turun.
        // KALAU ANGKANYA JUSTRU KEBALIK waktu didorong maju: tukar
        // REVERSE/FORWARD di baris yang berkaitan.
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

        telemetry.addLine("SIAP — tekan PLAY, lalu DORONG robotnya pakai tangan.");
        telemetry.addLine("Nggak perlu nyalain motor drivetrain sama sekali.");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //   Semua metode jalan TERUS sampai kamu tekan STOP.
        //
        // ====================================================================
        metode1_BacaTickMentah();
        //metode2_LacakNaif();
        //metode3_LacakDenganHeading();
        //metode4_LacakLengkap();
        // ====================================================================
    }

    // ========================================================================
    //   METODE 1 — BACA TICK MENTAH
    // ========================================================================
    /**
     * IDENYA:
     *   Cuma tampilkan angka mentah dari dua encoder. Belum ada
     *   perhitungan posisi sama sekali.
     *
     * KENAPA MULAI DI SINI:
     *   Sebelum percaya matematika rumit di metode 2-4, pastikan
     *   dulu HARDWARE-nya bener. Dorong robot MAJU — cuma "Tick
     *   maju" yang harusnya berubah banyak. Dorong ke SAMPING — cuma
     *   "Tick geser" yang harusnya berubah banyak. Kalau dua-duanya
     *   ikut berubah waktu kamu cuma dorong satu arah, kemungkinan
     *   pod-nya salah pasang/salah arah.
     */
    private void metode1_BacaTickMentah() {
        while (opModeIsActive()) {

            int tickMaju  = encoderMaju.getCurrentPosition();
            int tickGeser = encoderGeser.getCurrentPosition();

            telemetry.addData("METODE", "1 — Tick Mentah");
            telemetry.addData("Tick maju", tickMaju);
            telemetry.addData("Tick geser", tickGeser);
            telemetry.addData("Jarak maju", "%.2f inci", tickKeInci(tickMaju));
            telemetry.addData("Jarak geser", "%.2f inci", tickKeInci(tickGeser));
            telemetry.addLine();
            telemetry.addLine("Dorong maju/mundur, lalu dorong ke samping.");
            telemetry.addLine("Cek: apa cuma satu angka yang berubah tiap kali?");
            telemetry.update();
        }
    }

    // ========================================================================
    //   METODE 2 — LACAK NAIF (SALAH SENGAJA)
    // ========================================================================
    /**
     * IDENYA:
     *   Tiap loop, hitung berapa inci berubah sejak loop terakhir
     *   (delta), lalu TAMBAHKAN LANGSUNG ke x dan y global. Nggak
     *   ada IMU dilibatkan sama sekali.
     *
     * KENAPA INI SALAH:
     *   "Maju" dan "geser" itu SELALU relatif ke arah hadap ROBOT
     *   SAAT INI — bukan relatif ke lapangan. Begitu robot diputar,
     *   "maju" versi robot udah nunjuk ke arah lain di lapangan,
     *   tapi kode ini tetap menambahkannya ke sumbu X global seakan
     *   robot nggak pernah muter.
     *
     *   Ini kesalahan yang SAMA jenisnya dengan kenapa Auto03 perlu
     *   ambil getArah() di awal tiap jalanLurus() — cuma sekarang
     *   masalahnya muncul di ARAH LAIN (mengumpulkan posisi, bukan
     *   mengoreksi arah).
     *
     * BUKTIKAN SENDIRI:
     *   Dorong robot maju 24 inci, TANPA muter. X/Y di telemetry
     *   harusnya masuk akal. Sekarang dorong maju 24 inci, PUTAR
     *   robotnya 90 derajat di tempat, lalu dorong "maju" 24 inci
     *   lagi (yang sekarang searah sumbu lain di lapangan). Lihat
     *   X/Y-nya — robot nganggap kamu masih jalan di garis yang
     *   sama padahal aslinya belok.
     */
    private void metode2_LacakNaif() {

        double x = 0, y = 0;
        int tickMajuSebelumnya  = encoderMaju.getCurrentPosition();
        int tickGeserSebelumnya = encoderGeser.getCurrentPosition();

        while (opModeIsActive()) {

            int tickMajuSekarang  = encoderMaju.getCurrentPosition();
            int tickGeserSekarang = encoderGeser.getCurrentPosition();

            double deltaMaju  = tickKeInci(tickMajuSekarang - tickMajuSebelumnya);
            double deltaGeser = tickKeInci(tickGeserSekarang - tickGeserSebelumnya);

            tickMajuSebelumnya  = tickMajuSekarang;
            tickGeserSebelumnya = tickGeserSekarang;

            // SALAH: delta lokal ditambah langsung ke global, seakan
            // robot selalu menghadap arah yang sama kayak waktu mulai.
            x += deltaMaju;
            y += deltaGeser;

            telemetry.addData("METODE", "2 — Lacak Naif (SALAH SENGAJA)");
            telemetry.addData("Posisi", "X=%.2f  Y=%.2f inci", x, y);
            telemetry.addData("Arah IMU (diabaikan!)", "%.1f derajat", getArahDerajat());
            telemetry.update();
        }
    }

    // ========================================================================
    //   METODE 3 — LACAK DENGAN HEADING (MASIH ADA 1 CACAT)
    // ========================================================================
    /**
     * TAMBAHAN DARI METODE 2:
     *   Sebelum ditambahkan ke X/Y global, delta lokal (maju, geser)
     *   DIPUTAR dulu sesuai arah hadap robot, pakai rotasi 2D biasa:
     *
     *     deltaX = deltaMaju x cos(heading) - deltaGeser x sin(heading)
     *     deltaY = deltaMaju x sin(heading) + deltaGeser x cos(heading)
     *
     *   Dipakai heading RATA-RATA selama loop ini berlangsung (bukan
     *   cuma heading di akhir), supaya lebih akurat kalau robot lagi
     *   muter DAN maju bersamaan.
     *
     *   Ini langsung menyelesaikan masalah metode 2 — coba lagi
     *   percobaan "maju-belok-maju" dari metode 2, sekarang X/Y-nya
     *   harusnya jauh lebih masuk akal.
     *
     * CACAT YANG MASIH ADA — PUTAR DI TEMPAT:
     *   Pod GESER nggak persis di titik pusat rotasi robot — dia
     *   berjarak OFFSET_GESER_INCI dari situ. Waktu robot MUTER DI
     *   TEMPAT (tanpa geser beneran sama sekali), pod geser tetap
     *   ikut menyapu busur kecil karena dia "mengorbit" titik pusat.
     *   Kode ini belum tahu itu — dia anggap semua bacaan pod geser
     *   = geseran ASLI, padahal sebagian cuma efek putaran.
     *
     * BUKTIKAN SENDIRI:
     *   Taruh robot diam di satu titik, PUTAR di tempat 360 derajat
     *   pelan-pelan pakai tangan (jangan digeser sama sekali). X/Y
     *   di telemetry SEHARUSNYA tetap di sekitar 0,0 — tapi lihat,
     *   dia melenceng. Itu bukti cacatnya.
     */
    private void metode3_LacakDenganHeading() {

        double x = 0, y = 0;
        int tickMajuSebelumnya  = encoderMaju.getCurrentPosition();
        int tickGeserSebelumnya = encoderGeser.getCurrentPosition();
        double headingSebelumnya = getArahRadian();

        while (opModeIsActive()) {

            int tickMajuSekarang  = encoderMaju.getCurrentPosition();
            int tickGeserSekarang = encoderGeser.getCurrentPosition();
            double deltaMaju  = tickKeInci(tickMajuSekarang - tickMajuSebelumnya);
            double deltaGeser = tickKeInci(tickGeserSekarang - tickGeserSebelumnya);
            tickMajuSebelumnya  = tickMajuSekarang;
            tickGeserSebelumnya = tickGeserSekarang;

            double headingSekarang = getArahRadian();
            double deltaHeading = bedaSudutRadian(headingSekarang, headingSebelumnya);
            double headingRataRata = headingSebelumnya + (deltaHeading / 2.0);
            headingSebelumnya = headingSekarang;

            double deltaX = deltaMaju * Math.cos(headingRataRata) - deltaGeser * Math.sin(headingRataRata);
            double deltaY = deltaMaju * Math.sin(headingRataRata) + deltaGeser * Math.cos(headingRataRata);
            x += deltaX;
            y += deltaY;

            telemetry.addData("METODE", "3 — Dengan Heading (masih ada cacat)");
            telemetry.addData("Posisi", "X=%.2f  Y=%.2f inci", x, y);
            telemetry.addData("Arah", "%.1f derajat", getArahDerajat());
            telemetry.addLine();
            telemetry.addLine("Coba muter robot DI TEMPAT tanpa geser sama sekali.");
            telemetry.addLine("X/Y seharusnya diam di 0 — tapi nggak.");
            telemetry.update();
        }
    }

    // ========================================================================
    //   METODE 4 — LACAK LENGKAP (dikoreksi buat rotasi pod geser)
    // ========================================================================
    /**
     * TAMBAHAN DARI METODE 3:
     *   Sebelum dipakai, deltaGeser dikurangi dulu dengan busur palsu
     *   yang disebabkan murni oleh rotasi:
     *
     *     busurPalsu     = deltaHeading (radian) x OFFSET_GESER_INCI
     *     deltaGeserAsli = deltaGeserMentah - busurPalsu
     *
     *   Konsepnya sama seperti "keliling = jari-jari x sudut" yang
     *   sudah kamu pakai di Auto02 metode 2 — bedanya sekarang jari-
     *   jarinya adalah OFFSET_GESER_INCI (jarak pod ke pusat), dan
     *   ini dipakai buat MENGURANGI bacaan yang salah, bukan buat
     *   MENARGETKAN gerakan.
     *
     *   Sekarang muter di tempat nggak lagi bikin X/Y melenceng
     *   (selama OFFSET_GESER_INCI-nya benar), dan geseran ASLI tetap
     *   terbaca dengan benar. Ini localizer 2-wheel + IMU yang
     *   sungguhan dipakai, versi paling sederhananya.
     */
    private void metode4_LacakLengkap() {

        double x = 0, y = 0;
        int tickMajuSebelumnya  = encoderMaju.getCurrentPosition();
        int tickGeserSebelumnya = encoderGeser.getCurrentPosition();
        double headingSebelumnya = getArahRadian();

        while (opModeIsActive()) {

            int tickMajuSekarang  = encoderMaju.getCurrentPosition();
            int tickGeserSekarang = encoderGeser.getCurrentPosition();
            double deltaMaju        = tickKeInci(tickMajuSekarang - tickMajuSebelumnya);
            double deltaGeserMentah = tickKeInci(tickGeserSekarang - tickGeserSebelumnya);
            tickMajuSebelumnya  = tickMajuSekarang;
            tickGeserSebelumnya = tickGeserSekarang;

            double headingSekarang = getArahRadian();
            double deltaHeading = bedaSudutRadian(headingSekarang, headingSebelumnya);
            double headingRataRata = headingSebelumnya + (deltaHeading / 2.0);
            headingSebelumnya = headingSekarang;

            // Koreksi: buang bagian bacaan geser yang cuma efek muter di tempat.
            double busurPalsu = deltaHeading * OFFSET_GESER_INCI;
            double deltaGeser = deltaGeserMentah - busurPalsu;

            double deltaX = deltaMaju * Math.cos(headingRataRata) - deltaGeser * Math.sin(headingRataRata);
            double deltaY = deltaMaju * Math.sin(headingRataRata) + deltaGeser * Math.cos(headingRataRata);
            x += deltaX;
            y += deltaY;

            telemetry.addData("METODE", "4 — Lacak Lengkap");
            telemetry.addData("Posisi", "X=%.2f  Y=%.2f inci", x, y);
            telemetry.addData("Arah", "%.1f derajat", getArahDerajat());
            telemetry.addLine();
            telemetry.addLine("Coba muter di tempat lagi — X/Y harusnya tetap diam.");
            telemetry.update();
        }
    }

    // ========================================================================
    //   FUNGSI PEMBANTU
    // ========================================================================

    private double kelilingRodaEncoder() {
        return Math.PI * DIAMETER_RODA_ENCODER_INCI;
    }

    private double tickKeInci(int tick) {
        return (tick / TICK_PER_PUTARAN_ENCODER) * kelilingRodaEncoder();
    }

    private double getArahDerajat() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    private double getArahRadian() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
    }

    /** Selisih dua sudut RADIAN, dibereskan supaya selalu di antara -pi dan pi. */
    private double bedaSudutRadian(double a, double b) {
        double selisih = a - b;
        while (selisih >  Math.PI) selisih -= 2 * Math.PI;
        while (selisih < -Math.PI) selisih += 2 * Math.PI;
        return selisih;
    }
}

/*
 * ============================================================================
 *   LATIHAN — kerjakan berurutan
 * ============================================================================
 *
 *  PERCOBAAN 1 — Kalibrasi dasar
 *    a. Jalankan metode 1
 *    b. Dorong robot maju PERSIS 24 inci (ukur pakai meteran/tape)
 *    c. Bandingkan dengan "Jarak maju" di telemetry. Selisihnya
 *       seberapa jauh? Kalau meleset jauh, cek lagi
 *       DIAMETER_RODA_ENCODER_INCI.
 *
 *  PERCOBAAN 2 — Lihat metode naif gagal
 *    a. Jalankan metode 2
 *    b. Dorong maju 24", putar 90 derajat, dorong "maju" 24" lagi
 *    c. Apakah X/Y akhir masuk akal buat bentuk L yang kamu buat?
 *
 *  PERCOBAAN 3 — Buktikan metode 3 lebih baik, tapi belum sempurna
 *    a. Ulangi percobaan 2 pakai metode 3 — sekarang harusnya benar
 *    b. Taruh robot diam, putar 360 derajat DI TEMPAT (jangan geser
 *       sama sekali), balik ke arah semula
 *    c. Lihat X/Y — seharusnya balik ke (0,0) tapi meleset dikit.
 *       Itu busur palsu dari pod geser yang belum dikoreksi.
 *
 *  PERCOBAAN 4 — Buktikan metode 4 memperbaikinya
 *    a. Ulangi percobaan 3c pakai metode 4
 *    b. Selisihnya harusnya jauh lebih kecil sekarang
 *
 *  TANTANGAN — kalibrasi OFFSET_GESER_INCI pakai eksperimen
 *    a. Jalankan metode 3 (yang BELUM dikoreksi)
 *    b. Putar robot TEPAT 360 derajat di tempat (pelan-pelan, jaga
 *       supaya nggak geser)
 *    c. Catat seberapa jauh Y melenceng dari 0 (sebut angkanya "e")
 *    d. Satu putaran penuh = deltaHeading total 2*pi radian, jadi
 *       busur palsu totalnya = 2*pi x OFFSET_GESER_INCI. Itu artinya
 *       e (yang kamu catat) DEKAT dengan 2*pi x OFFSET_GESER_INCI.
 *       Hitung mundur OFFSET_GESER_INCI dari situ.
 *    e. Masukkan angka hasil hitunganmu, coba metode 4 lagi — lebih
 *       akurat dibanding tebakan penggaris di awal?
 * ============================================================================
 */
