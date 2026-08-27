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
 *   AUTO MAJU — 5 Cara Menyuruh Robot Jalan Lurus ke Depan
 * ============================================================================
 *
 *   Semua metode di file ini melakukan hal yang sama yaitu:
 *   menyuruh robot maju ke depan.
 *
 *   Bedanya cuma satu: presisi.
 *
 *   Metode 1 gampang tapi nggak bisa diandalkan.
 *   Metode 5 sedikit lebih ribet tapi hasilnya lebih konsisten.
 *
 *   Jangan langsung lompat ke metode 5. Coba dari nomor 1,
 *   ukur pakai meteran, dan rasakan sendiri masalahnya.
 *   Kalau kamu nggak pernah merasakan metode 1 gagal, kamu
 *   nggak akan benar-benar paham kenapa metode 2 ada.
 *
 *   ROBOT INI PAKAI:
 *     left_drive   -> Control Hub motor 0, REV HD Hex 20:1
 *     right_drive  -> Control Hub motor 1, REV HD Hex 20:1
 *     imu          -> sudah ada di dalam Control Hub
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Maju (Belajar)", group = "Belajar")
public class Auto01_MajuSimple extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    /**
     * TICK PER PUTARAN
     *
     * Di dalam motor ada sensor namanya ENCODER. Tugasnya cuma satu:
     * menghitung berapa jauh motor sudah berputar. Satuannya "tick".
     *
     * Motor REV HD Hex 20:1 mempunyai karakteristik berikut:
     *   - Motor mentahnya menghasilkan 28 tick per putaran
     *   - Gearbox 20:1 bikin poros keluar berputar 20x lebih lambat
     *   - Jadi: 28 x 20 = 560 tick untuk satu putaran roda
     *
     * KALAU KAMU GANTI MOTOR, ANGKA INI HARUS DIGANTI:
     *   REV HD Hex 20:1      ->  560
     *   REV HD Hex 40:1      ->  1120
     *   REV Core Hex         ->  288
     *   goBILDA 5203 312RPM  ->  537.7
     *   goBILDA 5203 435RPM  ->  384.5
     */
    private static final double TICK_PER_PUTARAN = 560.0;

    /**
     * DIAMETER RODA (INCI)
     *
     * UKUR PAKAI PENGGARIS. Jangan menebak.
     * Ukur dari sisi luar ke sisi luar, lewat titik tengah.
     *
     * Kalau angka ini salah 10%, jarak robot juga meleset 10%.
     */
    private static final double DIAMETER_RODA_INCI = 3.54;

    /**
     * KECEPATAN JALAN — dari 0.0 (diam) sampai 1.0 (ngebut)
     *
     * Mulai dari 0.1-0.3 dulu supaya robot pelan itu gampang diamati,
     * gampang dihentikan, dan nggak bikin rusak kalau salah kode.
     */
    private static final double KECEPATAN = 0.3;

    /**
     * KEKUATAN KOREKSI ARAH (dipakai di metode 5)
     *
     * Terlalu kecil -> robot tetap melenceng
     * Terlalu besar -> robot goyang kiri-kanan kayak ular
     *
     * 0.03 biasanya titik awal yang aman.
     */
    private static final double KEKUATAN_KOREKSI = 0.03;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================
    // Ini cuma "nama panggilan" di dalam kode. Belum terhubung ke apa pun.
    // Penyambungannya terjadi di runOpMode() di bawah.

    private DcMotor motorKiri;
    private DcMotor motorKanan;
    private IMU     imu;

    private final ElapsedTime waktu = new ElapsedTime();

    // ========================================================================
    //   BAGIAN 3 — PROGRAM UTAMA
    // ========================================================================

    @Override
    public void runOpMode() {

        // --------------------------------------------------------------
        //  3a. Sambungkan kode ke motor asli
        // --------------------------------------------------------------
        // hardwareMap itu semacam buku telepon. Kamu sebut namanya,
        // dia kasih motornya.
        //
        // Nama "left_drive" HARUS sama persis dengan yang kamu ketik
        // di Driver Hub waktu "Configure Robot". Beda huruf besar-kecil
        // pun nggak akan ketemu.

        motorKiri  = hardwareMap.get(DcMotor.class, "left_drive");
        motorKanan = hardwareMap.get(DcMotor.class, "right_drive");

        // --------------------------------------------------------------
        //  3b. Balik arah motor kiri
        // --------------------------------------------------------------
        // Motor kiri dan kanan dipasang saling berhadapan di robot.
        // Kalau dua-duanya dikasih power positif, robot malah muter
        // di tempat — yang satu maju, yang satu mundur.
        //
        // Dengan membalik motor kiri, sekarang "positif = maju"
        // untuk dua-duanya. Kode di bawah jadi jauh lebih sederhana.
        //
        // KALAU ROBOT KAMU MALAH MUNDUR: tukar REVERSE dan FORWARD
        // di dua baris ini.

        motorKiri.setDirection(DcMotor.Direction.REVERSE);
        motorKanan.setDirection(DcMotor.Direction.FORWARD);

        // --------------------------------------------------------------
        //  3c. Atur perilaku saat power = 0
        // --------------------------------------------------------------
        // BRAKE = motor langsung ngerem, robot berhenti di tempat
        // FLOAT = motor dilepas, robot masih meluncur beberapa cm
        //
        // Untuk autonomous, BRAKE selalu lebih baik.

        motorKiri.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorKanan.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // --------------------------------------------------------------
        //  3d. Siapkan IMU
        // --------------------------------------------------------------
        // IMU itu sensor arah yang SUDAH ADA di dalam Control Hub.
        // Gratis, nggak perlu beli, nggak perlu pasang kabel.
        // Dia bisa kasih tahu robot menghadap ke arah berapa derajat.
        //
        // Tapi dia perlu tahu Control Hub-nya dipasang bagaimana.
        // Lihat robot kamu:
        //   - Logo REV di Control Hub menghadap ke mana?
        //   - Port USB-nya menghadap ke mana?
        //
        // Pilihan: UP, DOWN, LEFT, RIGHT, FORWARD, BACKWARD
        //
        // KALAU SALAH: metode 5 akan mengoreksi ke arah yang salah,
        // dan robot malah muter-muter.

        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        )));

        // Anggap arah sekarang sebagai "nol derajat"
        imu.resetYaw();

        // --------------------------------------------------------------
        //  3e. Tampilkan info sebelum mulai
        // --------------------------------------------------------------
        // Semua kode di ATAS waitForStart() jalan waktu kamu tekan INIT.
        // Kode di BAWAHnya jalan waktu kamu tekan tombol PLAY.

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.addLine();
        telemetry.addData("Tick per putaran", TICK_PER_PUTARAN);
        telemetry.addData("Diameter roda", "%.2f inci", DIAMETER_RODA_INCI);
        telemetry.addData("1 putaran roda =", "%.2f inci", kelilingRoda());
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI
        //
        //   Aktifkan SATU baris saja.
        //   Baris yang lain kasih // di depannya biar dimatikan.
        //
        // ====================================================================
        metode1_Waktu(2.0);                   // jalan selama 2 detik
        //metode2_EncoderOtomatis(48);            // jalan 48 inci
        //metode3_EncoderManual(48);             // jalan 48 inci
        //metode4_PelanPelanBerhenti(48);       // jalan 48 inci, ngerem halus
        //metode5_LurusPakaiImu(48);           // jalan 48 inci, dijaga lurus
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(5000);
    }

    // ========================================================================
    //   METODE 1 — PAKAI WAKTU
    // ========================================================================
    /**
     * IDENYA:
     *   Nyalakan motor. Tunggu sekian detik. Matikan motor.
     *
     * KENAPA TERLIHAT MASUK AKAL:
     *   Karena memang begitu cara kerja mobil mainan. Gas 2 detik,
     *   ya jalan sekian meter.
     *
     * KENAPA SEBENARNYA BERMASALAH:
     *   setPower(0.3). Itu perintah
     *   "kasih 30% dari tegangan baterai ke motor".
     *
     *   Baterai penuh (13.5V) -> motor dapat 4.05V -> jalan cepat
     *   Baterai lemah (11.5V) -> motor dapat 3.45V -> jalan pelan
     *
     *   Padahal angka di kodenya sama persis.
     *
     *   Jadi autonomous kamu sempurna waktu latihan pagi (baterai
     *   baru di-charge), lalu meleset 20 cm di pertandingan ketiga.
     *   Ini bukan salah kamu. Metodenya yang memang nggak bisa
     *   diandalkan.
     *
     * BUKTIKAN SENDIRI:
     *   Jalankan ini 3x pakai baterai penuh, catat jaraknya.
     *   Lalu 3x lagi pakai baterai yang hampir habis.
     *   Bandingkan.
     */
    private void metode1_Waktu(double detik) {

        waktu.reset();   // mulai stopwatch dari nol

        motorKiri.setPower(KECEPATAN);
        motorKanan.setPower(KECEPATAN);

        // Terus berputar di sini sampai waktunya habis.
        // opModeIsActive() = cek apakah tombol STOP sudah ditekan.
        // SELALU pakai ini di setiap while, biar robot bisa dihentikan.
        while (opModeIsActive() && waktu.seconds() < detik) {
            telemetry.addData("METODE", "1 — Waktu");
            telemetry.addData("Berjalan", "%.2f / %.2f detik", waktu.seconds(), detik);
            telemetry.addLine();
            telemetry.addLine("Robot nggak tahu sudah jalan sejauh apa.");
            telemetry.addLine("Dia cuma menghitung waktu.");
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   METODE 2 — ENCODER, DIURUS OTOMATIS OLEH SDK
    // ========================================================================
    /**
     * IDENYA:
     *   Daripada menghitung waktu, hitung putaran roda.
     *
     * CARA HITUNGNYA:
     *   Keliling roda = 3.14 x diameter
     *                 = 3.14 x 9 cm
     *                 = 28.3 cm  (satu putaran roda = jalan 28.3 cm)
     *
     *   Mau jalan 100 cm?
     *     100 / 28.3 = 3.53 putaran
     *     3.53 x 560 tick = 1979 tick
     *
     *   Suruh motor berputar 1979 tick, selesai.
     *
     * KENAPA JAUH LEBIH BAIK:
     *   Baterai lemah nggak bikin jaraknya meleset. Robotnya cuma
     *   jalan lebih lambat, tapi tetap berhenti di titik yang sama.
     *   Encoder menghitung putaran asli, bukan perkiraan.
     *
     * RUN_TO_POSITION:
     *   Mode ini bikin SDK yang mengurus semuanya. Kamu bilang
     *   "pergi ke tick 1979", dia yang atur power-nya sendiri,
     *   termasuk memperlambat pas mau sampai.
     *
     *   Gampang dipakai. Tapi kamu nggak lihat apa yang terjadi
     *   di dalamnya. Makanya ada metode 3.
     *
     * KETELITIAN: sekitar 2 cm. Ini titik awal yang benar.
     */
    private void metode2_EncoderOtomatis(double jarakInci) {

        int targetTick = inciKeTick(jarakInci);

        // Langkah 1: nolkan hitungan encoder
        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // Langkah 2: kasih tahu mau ke tick berapa
        motorKiri.setTargetPosition(targetTick);
        motorKanan.setTargetPosition(targetTick);

        // Langkah 3: nyalakan mode otomatis
        // URUTANNYA PENTING — target harus di-set SEBELUM mode diganti
        motorKiri.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorKanan.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // Langkah 4: kasih power. Di mode ini, power = kecepatan MAKSIMAL.
        // SDK akan otomatis mengurangi power pas mendekati target.
        motorKiri.setPower(KECEPATAN);
        motorKanan.setPower(KECEPATAN);

        // isBusy() = "aku masih dalam perjalanan"
        while (opModeIsActive() && motorKiri.isBusy() && motorKanan.isBusy()) {
            telemetry.addData("METODE", "2 — Encoder Otomatis");
            telemetry.addData("Target", "%d tick (%.0f inci)", targetTick, jarakInci);
            telemetry.addData("Kiri sekarang",  motorKiri.getCurrentPosition());
            telemetry.addData("Kanan sekarang", motorKanan.getCurrentPosition());
            telemetry.addData("Sisa", "%d tick", targetTick - motorKiri.getCurrentPosition());
            telemetry.update();
        }

        berhenti();

        // Balikin ke mode biasa. Kalau lupa, kode berikutnya bisa
        // berperilaku aneh karena motornya masih di mode RUN_TO_POSITION.
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    // ========================================================================
    //   METODE 3 — ENCODER, DIURUS SENDIRI
    // ========================================================================
    /**
     * Hasilnya sama dengan metode 2. Bedanya: sekarang KAMU yang
     * memutuskan kapan berhenti, bukan SDK.
     *
     * KENAPA PERLU BELAJAR INI:
     *   RUN_TO_POSITION ke kotak hitam. Enak dipakai, tapi kamu
     *   nggak bisa mengubah apa pun di dalamnya.
     *
     *   Begitu kamu mau menambahkan sesuatu — misalnya "berhenti
     *   kalau sensor mendeteksi sesuatu", atau "pelan-pelan pas
     *   mau sampai", atau "jaga arah pakai IMU" — kamu WAJIB
     *   menulis loop-nya sendiri.
     *
     *   Metode 4 dan 5 dibangun di atas pola ini.
     *
     * POLANYA:
     *   1. Nolkan encoder
     *   2. Hitung target
     *   3. Nyalakan motor
     *   4. Loop: cek posisi. Sudah sampai? Kalau belum, lanjut.
     *   5. Matikan motor
     */
    private void metode3_EncoderManual(double jarakInci) {

        int targetTick = inciKeTick(jarakInci);

        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // RUN_USING_ENCODER = encoder tetap menghitung, tapi motor
        // nggak mengurus tujuan. Kamu yang pegang kendali.
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        motorKiri.setPower(KECEPATAN);
        motorKanan.setPower(KECEPATAN);

        while (opModeIsActive() && posisiRataRata() < targetTick) {

            telemetry.addData("METODE", "3 — Encoder Manual");
            telemetry.addData("Target", "%d tick", targetTick);
            telemetry.addData("Sekarang", "%d tick", posisiRataRata());
            telemetry.addData("Sudah jalan", "%.1f inci", tickKeInci(posisiRataRata()));
            telemetry.addLine();
            telemetry.addLine("Loop ini yang memutuskan kapan berhenti,");
            telemetry.addLine("bukan SDK.");
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   METODE 4 — PELAN-PELAN SEBELUM BERHENTI
    // ========================================================================
    /**
     * MASALAH DI METODE 3:
     *   Robot jalan kencang, lalu BRAK — berhenti mendadak tepat
     *   di target. Momentumnya bikin robot meluncur sedikit lewat
     *   garis. Makin cepat jalannya, makin jauh lewatnya.
     *
     * SOLUSINYA:
     *   Semakin dekat ke target, semakin kecil power-nya.
     *
     *     power = kecepatan x (sisa jarak / total jarak)
     *
     *   Di awal: sisa 100%, power penuh
     *   Setengah jalan: sisa 50%, power setengah
     *   Hampir sampai: sisa 5%, power sangat kecil
     *
     *   Robot mendekat pelan-pelan lalu berhenti mulus.
     *
     * INI NAMANYA KONTROL PROPORSIONAL.
     *   "Proporsional" artinya: besar koreksi sebanding dengan
     *   besar kesalahan. Jauh dari target -> usaha besar.
     *   Dekat target -> usaha kecil.
     *
     *   Ini huruf P dari PID. Kamu baru saja menulis controller
     *   pertama kamu.
     *
     * KENAPA ADA POWER_MINIMUM:
     *   Kalau power terlalu kecil, motor nggak kuat melawan
     *   gesekan dan robot berhenti sebelum sampai. Jadi kita
     *   kasih batas bawah.
     */
    private void metode4_PelanPelanBerhenti(double jarakInci) {

        final double POWER_MINIMUM = 0.15;   // di bawah ini motor nggak jalan

        int targetTick = inciKeTick(jarakInci);

        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        while (opModeIsActive() && posisiRataRata() < targetTick) {

            int sisa = targetTick - posisiRataRata();

            // Berapa persen perjalanan yang masih tersisa?
            double persenSisa = (double) sisa / targetTick;

            // Power sebanding dengan sisa jarak
            double power = KECEPATAN * persenSisa;

            // Jangan sampai terlalu kecil sampai motor nggak jalan
            power = Range.clip(power, POWER_MINIMUM, KECEPATAN);

            motorKiri.setPower(power);
            motorKanan.setPower(power);

            telemetry.addData("METODE", "4 — Pelan-Pelan Berhenti");
            telemetry.addData("Sudah jalan", "%.1f inci", tickKeInci(posisiRataRata()));
            telemetry.addData("Sisa", "%.1f inci", tickKeInci(sisa));
            telemetry.addData("Power sekarang", "%.3f", power);
            telemetry.addLine();
            telemetry.addLine("Lihat power-nya turun pelan-pelan.");
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   METODE 5 — DIJAGA LURUS PAKAI IMU
    // ========================================================================
    /**
     * MASALAH DI SEMUA METODE SEBELUMNYA:
     *   Coba jalan 200 cm pakai metode 3. Perhatikan robotnya.
     *   Dia nggak jalan lurus. Pasti melenceng sedikit ke satu sisi.
     *
     *   Penyebabnya:
     *     - Cengkeraman roda kiri dan kanan nggak pernah sama persis
     *     - Motor kiri dan kanan punya kekuatan sedikit berbeda
     *     - Karpetnya nggak rata
     *     - Berat robot nggak seimbang
     *
     *   Melenceng 3 derajat kelihatan sepele. Tapi setelah 3 meter,
     *   itu jadi 15 cm. Cukup untuk bikin auto kamu gagal.
     *
     * SOLUSINYA:
     *   Setiap saat, tanya IMU: "aku menghadap ke mana sekarang?"
     *   Bandingkan dengan arah yang seharusnya.
     *   Kalau melenceng ke kiri, kasih power lebih ke motor kiri.
     *
     *     error   = arah seharusnya - arah sekarang
     *     koreksi = error x KEKUATAN_KOREKSI
     *
     *     motorKiri  = kecepatan - koreksi
     *     motorKanan = kecepatan + koreksi
     *
     *   Sama seperti metode 4, ini kontrol proporsional.
     *   Bedanya: metode 4 mengoreksi JARAK, metode 5 mengoreksi ARAH.
     *
     * SOAL SUDUT -180 SAMPAI 180:
     *   IMU melaporkan sudut dari -180 sampai 180. Jadi kalau robot
     *   menghadap 179 derajat lalu bergeser 2 derajat, angkanya
     *   melompat ke -179. Selisihnya jadi terlihat 358 derajat,
     *   padahal cuma 2 derajat.
     *
     *   Dua baris "while" di bawah membereskan itu.
     *
     * COBA INI:
     *   Jalankan metode 5, lalu DORONG robotnya pelan ke samping
     *   waktu sedang jalan. Lihat dia mengoreksi sendiri.
     */
    private void metode5_LurusPakaiImu(double jarakInci) {

        int targetTick = inciKeTick(jarakInci);

        // Arah robot SEKARANG dianggap sebagai "lurus"
        double arahTarget = getArah();

        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        while (opModeIsActive() && posisiRataRata() < targetTick) {

            // Seberapa melenceng dari arah seharusnya?
            double error = arahTarget - getArah();

            // Bereskan lompatan 180 / -180
            while (error >  180) error -= 360;
            while (error < -180) error += 360;

            double koreksi = error * KEKUATAN_KOREKSI;

            // Satu sisi dikurangi, satu sisi ditambah -> robot membelok
            // sedikit ke arah yang benar
            double powerKiri  = Range.clip(KECEPATAN - koreksi, -1.0, 1.0);
            double powerKanan = Range.clip(KECEPATAN + koreksi, -1.0, 1.0);

            motorKiri.setPower(powerKiri);
            motorKanan.setPower(powerKanan);

            telemetry.addData("METODE", "5 — Lurus Pakai IMU");
            telemetry.addData("Sudah jalan", "%.1f inci", tickKeInci(posisiRataRata()));
            telemetry.addLine();
            telemetry.addData("Arah seharusnya", "%.1f derajat", arahTarget);
            telemetry.addData("Arah sekarang", "%.1f derajat", getArah());
            telemetry.addData("Melenceng", "%.2f derajat", error);
            telemetry.addData("Koreksi", "%.3f", koreksi);
            telemetry.addLine();
            telemetry.addData("Power", "Kiri %.3f | Kanan %.3f", powerKiri, powerKanan);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   FUNGSI PEMBANTU
    // ========================================================================
    // Fungsi-fungsi kecil ini dipakai berulang kali di atas.
    // Menulis sekali di sini jauh lebih baik daripada copy-paste
    // rumus yang sama di lima tempat berbeda.

    /** Keliling roda dalam INCI. Satu putaran roda = sejauh ini. */
    private double kelilingRoda() {
        return Math.PI * DIAMETER_RODA_INCI;
    }

    /** Ubah jarak (INCI) menjadi jumlah tick encoder. */
    private int inciKeTick(double inci) {
        return (int) ((inci / kelilingRoda()) * TICK_PER_PUTARAN);
    }

    /** Ubah tick encoder menjadi jarak (INCI). Kebalikan dari InciKeTick. */
    private double tickKeInci(int tick) {
        return (tick / TICK_PER_PUTARAN) * kelilingRoda();
    }

    /**
     * Rata-rata posisi dua motor.
     *
     * Kenapa dirata-rata? Karena kalau robot sedikit melenceng,
     * satu roda jalan lebih jauh dari yang lain. Rata-ratanya
     * lebih mendekati jarak asli yang ditempuh badan robot.
     */
    private int posisiRataRata() {
        return (Math.abs(motorKiri.getCurrentPosition())
                + Math.abs(motorKanan.getCurrentPosition())) / 2;
    }

    /** Arah hadap robot dalam derajat. Belok kiri = positif. */
    private double getArah() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
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
 *  PERCOBAAN 1 — Buktikan metode 1 nggak bisa diandalkan
 *    a. Charge baterai sampai penuh
 *    b. Jalankan metode 1, ukur jaraknya pakai meteran, catat
 *    c. Ulangi 3 kali
 *    d. Pakai baterai yang hampir habis, ulangi 3 kali lagi
 *    e. Berapa selisih terbesarnya?
 *
 *  PERCOBAAN 2 — Bandingkan dengan encoder
 *    a. Ulangi percobaan 1, tapi pakai metode 2
 *    b. Sekarang selisihnya berapa?
 *
 *  PERCOBAAN 3 — Rusak dengan sengaja
 *    a. Ganti TICK_PER_PUTARAN jadi 280 (setengahnya)
 *    b. TEBAK DULU apa yang akan terjadi, tulis tebakanmu
 *    c. Jalankan metode 2. Tebakanmu benar?
 *    d. Kembalikan ke 560
 *
 *  PERCOBAAN 4 — Lihat pengaruh diameter roda
 *    a. Ganti DIAMETER_RODA_CM jadi dua kali lipatnya
 *    b. Tebak dulu, lalu jalankan metode 2
 *    c. Kembalikan ke angka aslinya
 *
 *  PERCOBAAN 5 — Rasakan momentum
 *    a. Naikkan KECEPATAN jadi 0.8
 *    b. Jalankan metode 3, ukur seberapa lewat dari target
 *    c. Jalankan metode 4 dengan kecepatan yang sama
 *    d. Mana yang lebih tepat?
 *
 *  PERCOBAAN 6 — Lihat robot melenceng
 *    a. Jalankan metode 3 sejauh 50 inci
 *    b. Ukur berapa cm robot melenceng ke samping
 *    c. Jalankan metode 5, ukur lagi
 *
 *  PERCOBAAN 7 — Rusak koreksinya
 *    a. Ganti KEKUATAN_KOREKSI jadi 0.20
 *    b. Jalankan metode 5. Kenapa robotnya goyang-goyang?
 *    c. Ganti jadi 0.001. Kenapa sekarang nggak ada bedanya?
 *    d. Cari angka terbaik untuk robot kamu
 *
 *  TANTANGAN
 *    a. Tulis fungsi mundur(). Apa yang perlu diubah?
 *    b. Gabungkan metode 4 dan 5 — pelan-pelan berhenti
 *       SEKALIGUS dijaga lurus
 *
 * ============================================================================
 */