package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * ============================================================================
 *   AUTO MESIN KEADAAN — Mengerjakan Dua Hal "Sekaligus"
 * ============================================================================
 *
 *   Auto01-08 SEMUANYA pakai pola yang sama: satu while-loop BLOCKING
 *   per gerakan (jalanLurus(), belok(), dst). "Blocking" artinya kode
 *   di baris SETELAHNYA nggak akan jalan sama sekali sampai loop itu
 *   selesai total. Ini gampang dipahami, tapi ada harganya: kalau kamu
 *   punya DUA hal yang perlu terjadi (misalnya jalan maju SEKALIGUS
 *   buka capit), kode blocking cuma bisa ngerjain satu-satu, bergantian.
 *
 *   File ini nggak mengajarkan gerakan baru — motor dan servo yang
 *   dipakai sama persis dengan Auto01 dan Auto06. Yang baru adalah
 *   CARA MENYUSUN KODE-nya, supaya beberapa hal bisa jalan bersamaan
 *   dalam SATU loop, bukan berurutan di banyak loop terpisah.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive -> sama Auto01
 *     servo_lengan             -> sama Auto06
 *
 *   Sengaja TIDAK pakai IMU/belokPID presisi di sini — topik file ini
 *   soal ARSITEKTUR KODE, bukan soal akurasi gerakan. Belokannya
 *   dibikin paling sederhana (kayak Auto02 metode 1) supaya nggak
 *   mengalihkan perhatian dari inti pelajaran.
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Mesin Keadaan (Belajar)", group = "Belajar")
public class Auto09_MesinKeadaan extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    private static final double TICK_PER_PUTARAN   = 560.0;
    private static final double DIAMETER_RODA_INCI = 3.54;
    private static final double KECEPATAN = 0.3;

    private static final double POSISI_TUTUP = 0.0;
    private static final double POSISI_BUKA  = 1.0;
    private static final double LANGKAH_PER_LOOP = 0.01;  // dari Auto06
    private static final int    JEDA_LANGKAH_MS  = 15;    // dari Auto06

    private static final double KECEPATAN_BELOK   = 0.3;
    private static final double WAKTU_BELOK_DETIK = 1.0;  // belok sederhana, berbasis waktu

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor motorKiri;
    private DcMotor motorKanan;
    private Servo   servoLengan;

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

        servoLengan = hardwareMap.get(Servo.class, "servo_lengan");
        servoLengan.setPosition(POSISI_TUTUP);

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_Berurutan();
        //metode2_Konkuren();
        //metode3_MesinKeadaanBerfase();
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(3000);
    }

    // ========================================================================
    //   METODE 1 — BERURUTAN (BLOCKING, SEPERTI AUTO01-08)
    // ========================================================================
    /**
     * IDENYA:
     *   Jalan maju 24 inci (tunggu sampai selesai TOTAL), BARU SETELAH
     *   ITU gerakkan servo dari tertutup ke terbuka (tunggu lagi
     *   sampai selesai). Dua hal, dua loop terpisah, satu demi satu.
     *
     * KENAPA INI BOROS WAKTU:
     *   Total waktu = waktu jalan + waktu servo. Kalau jalan makan
     *   waktu 2 detik dan servo makan waktu 1.5 detik, totalnya 3.5
     *   detik — padahal SECARA FISIK nggak ada alasan robot nggak
     *   bisa jalan maju SAMBIL lengannya bergerak. Dua-duanya nggak
     *   saling ganggu (motor drivetrain dan servo lengan itu hardware
     *   yang beda, nggak rebutan apa pun).
     *
     *   Di kompetisi, autonomous cuma dikasih waktu TERBATAS (misalnya
     *   30 detik). Tiap detik yang kebuang percuma karena kode ditulis
     *   berurutan padahal bisa bersamaan = kesempatan yang hilang.
     */
    private void metode1_Berurutan() {

        ElapsedTime waktuTotal = new ElapsedTime();

        // --- Tugas A: jalan maju, TUNGGU sampai selesai total ---
        int targetTick = inciKeTick(24);
        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKiri.setPower(KECEPATAN);
        motorKanan.setPower(KECEPATAN);
        while (opModeIsActive() && posisiRataRata() < targetTick) {
            telemetry.addData("METODE", "1 — Berurutan");
            telemetry.addData("Tugas A (jalan)", "SEDANG JALAN");
            telemetry.addData("Tugas B (servo)", "belum mulai sama sekali");
            telemetry.update();
        }
        berhenti();

        // --- Tugas B: BARU dimulai setelah Tugas A 100% selesai ---
        double posisiSekarang = servoLengan.getPosition();
        while (opModeIsActive() && Math.abs(POSISI_BUKA - posisiSekarang) > LANGKAH_PER_LOOP) {
            posisiSekarang += LANGKAH_PER_LOOP;
            servoLengan.setPosition(posisiSekarang);
            telemetry.addData("METODE", "1 — Berurutan");
            telemetry.addData("Tugas A (jalan)", "SUDAH SELESAI");
            telemetry.addData("Tugas B (servo)", "SEDANG JALAN");
            telemetry.update();
            sleep(JEDA_LANGKAH_MS);
        }
        servoLengan.setPosition(POSISI_BUKA);

        telemetry.addData("TOTAL WAKTU", "%.2f detik", waktuTotal.seconds());
        telemetry.update();
    }

    // ========================================================================
    //   METODE 2 — KONKUREN (SATU LOOP, DUA TUGAS SEKALIGUS)
    // ========================================================================
    /**
     * IDENYA:
     *   Gabung jadi SATU while-loop. Tiap kali loop berputar, dia
     *   mengerjakan SEDIKIT kemajuan Tugas A DAN sedikit kemajuan
     *   Tugas B, bukan menghabiskan satu tugas dulu baru pindah.
     *
     *   Loop berhenti waktu KEDUANYA sudah selesai (motorSelesai DAN
     *   servoSelesai sama-sama true) — bukan waktu SALAH SATU selesai.
     *
     * KENAPA sleep() NGGAK BOLEH DIPAKAI DI SINI:
     *   Auto06 metode 2 pakai sleep(JEDA_LANGKAH_MS) buat mengatur
     *   kecepatan gerak servo. Itu OK waktu servo satu-satunya hal
     *   yang terjadi. Tapi sleep() MENGHENTIKAN SELURUH THREAD —
     *   kalau dipanggil di sini, motor drivetrain JUGA ikut berhenti
     *   ngapa-ngapain selama itu, karena kode robot cuma py satu
     *   "jalur eksekusi". Makanya servo di sini dijeda pakai
     *   ElapsedTime (cek "apa sudah JEDA_LANGKAH_MS berlalu?") alih-
     *   alih sleep() — supaya loop tetap SEMPAT mengurus Tugas A di
     *   iterasi yang sama.
     *
     * KENAPA INI LEBIH CEPAT:
     *   Total waktu sekarang mendekati MAX(waktu jalan, waktu servo),
     *   bukan JUMLAH keduanya. Bandingkan angka "TOTAL WAKTU" di
     *   telemetry dengan hasil metode 1.
     */
    private void metode2_Konkuren() {

        ElapsedTime waktuTotal = new ElapsedTime();
        ElapsedTime waktuServoLoop = new ElapsedTime();

        int targetTick = inciKeTick(24);
        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        double posisiServoSekarang = servoLengan.getPosition();
        boolean motorSelesai = false;
        boolean servoSelesai = false;

        while (opModeIsActive() && !(motorSelesai && servoSelesai)) {

            // --- Tugas A: motor. SEDIKIT kerjaan tiap loop, bukan tunggu. ---
            if (!motorSelesai) {
                if (posisiRataRata() < targetTick) {
                    motorKiri.setPower(KECEPATAN);
                    motorKanan.setPower(KECEPATAN);
                } else {
                    berhenti();
                    motorSelesai = true;
                }
            }

            // --- Tugas B: servo. Dijeda pakai timer, BUKAN sleep(). ---
            if (!servoSelesai) {
                if (waktuServoLoop.milliseconds() >= JEDA_LANGKAH_MS) {
                    waktuServoLoop.reset();
                    if (Math.abs(POSISI_BUKA - posisiServoSekarang) > LANGKAH_PER_LOOP) {
                        posisiServoSekarang += LANGKAH_PER_LOOP;
                        servoLengan.setPosition(posisiServoSekarang);
                    } else {
                        servoLengan.setPosition(POSISI_BUKA);
                        servoSelesai = true;
                    }
                }
            }

            telemetry.addData("METODE", "2 — Konkuren");
            telemetry.addData("Tugas A (jalan)", motorSelesai ? "SELESAI" : "sedang jalan");
            telemetry.addData("Tugas B (servo)", servoSelesai ? "SELESAI" : "sedang jalan");
            telemetry.update();
        }

        telemetry.addData("TOTAL WAKTU", "%.2f detik", waktuTotal.seconds());
        telemetry.update();
    }

    // ========================================================================
    //   METODE 3 — MESIN KEADAAN BERFASE (POLA SUNGGUHAN)
    // ========================================================================
    /**
     * metode2_Konkuren nunjukkin idenya buat DUA tugas sekaligus. Tapi
     * autonomous sungguhan biasanya butuh BANYAK fase yang harus
     * terjadi BERURUTAN (fase 2 nggak boleh mulai sebelum fase 1
     * selesai), sementara DI DALAM tiap fase, mungkin ada beberapa
     * tugas yang boleh konkuren.
     *
     *   FASE_MAJU_BUKA  -> jalan maju 24" DAN buka servo, konkuren
     *                       (persis metode 2)
     *   FASE_BELOK      -> muter 90 derajat (sendirian, nggak ada
     *                       tugas lain bersamaan di fase ini)
     *   FASE_MUNDUR_TUTUP -> mundur 12" DAN tutup servo, konkuren lagi
     *   FASE_SELESAI    -> berhenti total
     *
     *   enum Tahap {...} dipakai buat menandai "kita lagi di fase
     *   yang mana". switch(tahap) tiap loop ngecek fase SEKARANG,
     *   kerjain tugas-tugasnya, dan PINDAH ke tahap berikutnya kalau
     *   semua tugas fase itu sudah selesai.
     *
     * KENAPA POLA INI YANG DIPAKAI KODE FTC SUNGGUHAN:
     *   Ini "mesin keadaan" (state machine) beneran — satu variabel
     *   nyimpen KEADAAN SEKARANG, dan tiap loop cuma ngerjain
     *   pekerjaan buat keadaan itu, lalu (kalau syaratnya kena)
     *   pindah keadaan. Nggak ada while-loop bersarang, nggak ada
     *   blocking — semuanya satu loop besar yang jalan terus dari
     *   waitForStart() sampai match selesai.
     */
    private enum Tahap { MAJU_BUKA, BELOK, MUNDUR_TUTUP, SELESAI }

    private void metode3_MesinKeadaanBerfase() {

        Tahap tahap = Tahap.MAJU_BUKA;

        int targetTickMaju = inciKeTick(24);
        int targetTickMundur = inciKeTick(12);
        double posisiServoSekarang = servoLengan.getPosition();
        boolean motorSelesai = false;
        boolean servoSelesai = false;

        ElapsedTime waktuFase = new ElapsedTime();
        ElapsedTime waktuServoLoop = new ElapsedTime();

        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        while (opModeIsActive() && tahap != Tahap.SELESAI) {

            switch (tahap) {

                case MAJU_BUKA:
                    if (!motorSelesai) {
                        if (posisiRataRata() < targetTickMaju) {
                            motorKiri.setPower(KECEPATAN);
                            motorKanan.setPower(KECEPATAN);
                        } else {
                            berhenti();
                            motorSelesai = true;
                        }
                    }
                    if (!servoSelesai && waktuServoLoop.milliseconds() >= JEDA_LANGKAH_MS) {
                        waktuServoLoop.reset();
                        if (Math.abs(POSISI_BUKA - posisiServoSekarang) > LANGKAH_PER_LOOP) {
                            posisiServoSekarang += LANGKAH_PER_LOOP;
                            servoLengan.setPosition(posisiServoSekarang);
                        } else {
                            servoLengan.setPosition(POSISI_BUKA);
                            servoSelesai = true;
                        }
                    }
                    if (motorSelesai && servoSelesai) {
                        // Reset penanda buat dipakai ULANG di fase berikutnya.
                        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        motorSelesai = false;
                        servoSelesai = false;
                        waktuFase.reset();
                        tahap = Tahap.BELOK;
                    }
                    break;

                case BELOK:
                    if (waktuFase.seconds() < WAKTU_BELOK_DETIK) {
                        motorKiri.setPower(-KECEPATAN_BELOK);
                        motorKanan.setPower(KECEPATAN_BELOK);
                    } else {
                        berhenti();
                        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        tahap = Tahap.MUNDUR_TUTUP;
                    }
                    break;

                case MUNDUR_TUTUP:
                    if (!motorSelesai) {
                        if (posisiRataRata() < targetTickMundur) {
                            motorKiri.setPower(-KECEPATAN);
                            motorKanan.setPower(-KECEPATAN);
                        } else {
                            berhenti();
                            motorSelesai = true;
                        }
                    }
                    if (!servoSelesai && waktuServoLoop.milliseconds() >= JEDA_LANGKAH_MS) {
                        waktuServoLoop.reset();
                        if (Math.abs(POSISI_TUTUP - posisiServoSekarang) > LANGKAH_PER_LOOP) {
                            posisiServoSekarang -= LANGKAH_PER_LOOP;
                            servoLengan.setPosition(posisiServoSekarang);
                        } else {
                            servoLengan.setPosition(POSISI_TUTUP);
                            servoSelesai = true;
                        }
                    }
                    if (motorSelesai && servoSelesai) {
                        tahap = Tahap.SELESAI;
                    }
                    break;

                case SELESAI:
                    break;
            }

            telemetry.addData("METODE", "3 — Mesin Keadaan Berfase");
            telemetry.addData("TAHAP SEKARANG", tahap);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   FUNGSI PEMBANTU
    // ========================================================================

    private double kelilingRoda() {
        return Math.PI * DIAMETER_RODA_INCI;
    }

    private int inciKeTick(double inci) {
        return (int) ((inci / kelilingRoda()) * TICK_PER_PUTARAN);
    }

    private int posisiRataRata() {
        return (Math.abs(motorKiri.getCurrentPosition())
                + Math.abs(motorKanan.getCurrentPosition())) / 2;
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
 *  PERCOBAAN 1 — Ukur penghematan waktunya
 *    a. Jalankan metode 1, catat "TOTAL WAKTU"
 *    b. Jalankan metode 2, catat "TOTAL WAKTU"
 *    c. Selisihnya seberapa besar? Apa mendekati waktu yang lebih
 *       PENDEK dari dua tugas itu (bukan jumlahnya)?
 *
 *  PERCOBAAN 2 — Lihat satu tugas selesai duluan
 *    a. Di metode 2, ganti target jalan jadi 6 inci (jauh lebih
 *       pendek dari waktu servo buka penuh)
 *    b. Jalankan. Perhatikan telemetry — "Tugas A (jalan)" harusnya
 *       ke "SELESAI" duluan, sementara "Tugas B (servo)" masih
 *       "sedang jalan". Loop tetap lanjut sampai KEDUANYA selesai.
 *
 *  PERCOBAAN 3 — Rusak dengan sengaja
 *    a. Di metode 2, ganti ElapsedTime.milliseconds() jadi sleep()
 *       biasa buat jeda servo (kayak metode 1)
 *    b. Jalankan. Apa Tugas A (motor) masih kelihatan "sedang jalan"
 *       terus-menerus di telemetry selama servo bergerak, atau
 *       malah macet nunggu?
 *
 *  TANTANGAN
 *    a. Ubah belokPID() dari Auto05_GabunganPID supaya NON-BLOCKING:
 *       daripada while-loop yang nunggu sampai selesai, buat dia jadi
 *       fungsi yang dipanggil SEKALI PER LOOP dan MENGEMBALIKAN
 *       boolean (true kalau sudah sampai target, false kalau belum).
 *       Ini pola yang SUNGGUHAN dipakai kode FTC tingkat lanjut —
 *       semua fungsi gerakan (jalanLurusPID, belokPID, pergiKeTitik)
 *       ditulis ulang jadi bentuk "satu langkah per panggilan" biar
 *       bisa dipakai di dalam mesin keadaan macam metode 3 di atas.
 * ============================================================================
 */
