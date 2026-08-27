package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * ============================================================================
 *   AUTO PILIH RUTE — Menu Sebelum PLAY, Buat Hari Pertandingan
 * ============================================================================
 *
 *   SEMUA file Auto01-15 punya pola yang sama di bagian "PILIH METODE
 *   DI SINI": kamu comment/uncomment satu baris kode, lalu BUILD ULANG
 *   dan DEPLOY ULANG ke robot, buat ganti apa yang dijalankan.
 *
 *   Itu OK waktu latihan. Tapi di HARI PERTANDINGAN, kamu SERING BARU
 *   TAHU beberapa detik sebelum match: robot ditaruh wasit di sisi
 *   KIRI atau KANAN lapangan, dan aliansimu minta kamu mulai TERLAMBAT
 *   beberapa detik supaya nggak tabrakan sama robot partner aliansi
 *   yang jalan duluan. Nggak ada waktu buat colok laptop, ubah kode,
 *   build, deploy — itu semua makan waktu MENIT, sementara kamu cuma
 *   punya HITUNGAN DETIK di antara robot ditaruh dan wasit bilang
 *   "3...2...1...GO."
 *
 *   SOLUSINYA: bikin MENU yang bisa dipilih pakai GAMEPAD, dijalankan
 *   SEBELUM tombol PLAY ditekan (selama fase INIT). Gamepad tetap bisa
 *   dibaca di fase ini — waitForStart() itu yang BLOCKING, tapi kode
 *   SEBELUM waitForStart() bebas kamu isi loop sendiri yang baca
 *   gamepad kapan pun kamu mau.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive -> drivetrain sederhana, fokus
 *                                 pelajaran ini di menu-nya, bukan di
 *                                 presisi gerakannya
 *
 *   Ini penutup Arc 2 (Auto13-16: cara nulis, tuning, dan mengatur
 *   kode). Arc 1 (Auto01-12) sebelumnya soal apa saja yang bisa
 *   dilakukan robot secara fisik.
 *
 *   LANJUT KE: Auto17_ProfilGerak — Arc 3 dimulai di sana: bukan
 *   soal robot BISA ngapain lagi (Arc 1) atau kodenya diatur gimana
 *   (Arc 2), tapi soal GERAKANNYA sendiri dibikin lebih halus,
 *   lebih cepat, dan lebih bisa dipercaya.
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Pilih Rute (Belajar)", group = "Belajar")
public class Auto16_PilihRute extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    private static final double KECEPATAN = 0.3;
    private static final double WAKTU_GERAK_DETIK = 1.5;
    private static final int DELAY_MAKS_DETIK = 10;

    private enum Rute { KIRI, KANAN, PARKIR }

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
        motorKiri.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorKanan.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_HardcodedLamaCara();
        //metode2_MenuInit();
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(3000);
    }

    // ========================================================================
    //   METODE 1 — CARA LAMA: HARDCODE, GANTI = BUILD ULANG
    // ========================================================================
    /**
     * IDENYA:
     *   Rute-nya TETAP, ditulis LANGSUNG di kode. Mau ganti ke rute
     *   lain? Ubah baris di bawah ini, build ulang, deploy ulang.
     *
     * KENAPA INI DITULIS ULANG DI SINI:
     *   Ini POLA YANG SAMA yang dipakai SETIAP file Auto01-15 buat
     *   memilih "metode" — comment/uncomment, build ulang. Berguna
     *   waktu belajar/latihan (kamu PUNYA waktu buat itu), tapi
     *   nggak praktis di hari pertandingan seperti dijelaskan di
     *   banner atas.
     */
    private void metode1_HardcodedLamaCara() {

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.addLine("(rute sudah di-hardcode: KIRI, delay 0 detik)");
        telemetry.update();

        waitForStart();

        jalankanRute(Rute.KIRI);
    }

    // ========================================================================
    //   METODE 2 — MENU DI FASE INIT (SEBELUM PLAY)
    // ========================================================================
    /**
     * IDENYA:
     *   Sebelum manggil waitForStart(), jalankan LOOP SENDIRI yang
     *   baca tombol gamepad terus-menerus, ubah pilihan rute/delay
     *   berdasarkan tombol yang ditekan, dan tampilkan pilihan
     *   sekarang di telemetry. Loop ini otomatis berhenti begitu
     *   driver menekan PLAY (isStarted() jadi true) atau STOP.
     *
     * KENAPA PERLU "EDGE DETECTION" (deteksi TEPI PENEKANAN):
     *   Loop ini jalan PULUHAN kali per detik. Kalau kamu cuma cek
     *   "if (gamepad1.dpad_right) pindah ke rute berikutnya", selama
     *   tombolnya DITAHAN (bahkan cuma sepersekian detik), kode itu
     *   akan "pindah rute berikutnya" PULUHAN KALI dalam sekejap —
     *   nggak mungkin dikontrol manusia.
     *
     *   Solusinya: simpan status tombol LOOP SEBELUMNYA. Cuma
     *   bertindak waktu tombolnya BARU SAJA berubah dari "belum
     *   ditekan" ke "ditekan" (disebut RISING EDGE / tepi naik) —
     *   bukan setiap kali dia dalam keadaan "sedang ditekan".
     *
     * KENAPA DELAY PENTING:
     *   Beberapa strategi aliansi butuh satu robot mulai duluan,
     *   yang lain nunggu beberapa detik supaya nggak saling
     *   menghalangi jalur. sleep(delayDetik * 1000) SETELAH
     *   waitForStart() itu caranya — robot "diam dengar" sampai
     *   delay-nya habis, baru mulai rute sungguhan.
     */
    private void metode2_MenuInit() {

        Rute ruteTerpilih = Rute.KIRI;
        int delayDetik = 0;

        boolean dpadKiriSebelumnya = false;
        boolean dpadKananSebelumnya = false;
        boolean dpadAtasSebelumnya = false;
        boolean dpadBawahSebelumnya = false;

        while (!isStarted() && !isStopRequested()) {

            boolean dpadKiriSekarang = gamepad1.dpad_left;
            boolean dpadKananSekarang = gamepad1.dpad_right;
            boolean dpadAtasSekarang = gamepad1.dpad_up;
            boolean dpadBawahSekarang = gamepad1.dpad_down;

            // Rising edge: cuma bertindak PAS SAAT tombol BARU ditekan.
            if (dpadKananSekarang && !dpadKananSebelumnya) {
                ruteTerpilih = ruteBerikutnya(ruteTerpilih);
            }
            if (dpadKiriSekarang && !dpadKiriSebelumnya) {
                ruteTerpilih = ruteSebelumnya(ruteTerpilih);
            }
            if (dpadAtasSekarang && !dpadAtasSebelumnya) {
                delayDetik = (int) Range.clip(delayDetik + 1, 0, DELAY_MAKS_DETIK);
            }
            if (dpadBawahSekarang && !dpadBawahSebelumnya) {
                delayDetik = (int) Range.clip(delayDetik - 1, 0, DELAY_MAKS_DETIK);
            }

            dpadKiriSebelumnya = dpadKiriSekarang;
            dpadKananSebelumnya = dpadKananSekarang;
            dpadAtasSebelumnya = dpadAtasSekarang;
            dpadBawahSebelumnya = dpadBawahSekarang;

            telemetry.addLine("=== MENU AUTONOMOUS ===");
            telemetry.addData("Rute (dpad KIRI/KANAN)", ruteTerpilih);
            telemetry.addData("Delay (dpad ATAS/BAWAH)", "%d detik", delayDetik);
            telemetry.addLine();
            telemetry.addLine("Tekan PLAY kalau sudah yakin.");
            telemetry.update();
        }

        if (opModeIsActive()) {
            telemetry.addData("Menunggu delay", "%d detik...", delayDetik);
            telemetry.update();
            sleep((long) delayDetik * 1000);

            jalankanRute(ruteTerpilih);
        }
    }

    private Rute ruteBerikutnya(Rute sekarang) {
        Rute[] semua = Rute.values();
        return semua[(sekarang.ordinal() + 1) % semua.length];
    }

    private Rute ruteSebelumnya(Rute sekarang) {
        Rute[] semua = Rute.values();
        return semua[(sekarang.ordinal() - 1 + semua.length) % semua.length];
    }

    // ========================================================================
    //   RUTE-RUTE YANG SUDAH DISIAPKAN
    // ========================================================================
    /**
     * Sengaja pakai gerakan sederhana berbasis waktu (bukan PID/
     * odometri) supaya fokus pelajaran ini tetap di MENU-nya. Ganti
     * isi tiap rute ini dengan belokPID()/pergiKeTitik() dari Auto05/
     * 08 buat robot kompetisi sungguhan.
     */
    private void jalankanRute(Rute rute) {
        telemetry.addData("Menjalankan rute", rute);
        telemetry.update();

        switch (rute) {
            case KIRI:
                gerakSelama(KECEPATAN, KECEPATAN, WAKTU_GERAK_DETIK);
                gerakSelama(-KECEPATAN, KECEPATAN, 1.0);
                break;
            case KANAN:
                gerakSelama(KECEPATAN, KECEPATAN, WAKTU_GERAK_DETIK);
                gerakSelama(KECEPATAN, -KECEPATAN, 1.0);
                break;
            case PARKIR:
                gerakSelama(KECEPATAN, KECEPATAN, WAKTU_GERAK_DETIK / 2);
                break;
        }
        berhenti();
    }

    private void gerakSelama(double powerKiri, double powerKanan, double detik) {
        ElapsedTime waktu = new ElapsedTime();
        while (opModeIsActive() && waktu.seconds() < detik) {
            motorKiri.setPower(powerKiri);
            motorKanan.setPower(powerKanan);
        }
        berhenti();
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
 *  PERCOBAAN 1 — Rasakan bedanya
 *    a. Jalankan metode 1, lihat betapa nggak fleksibelnya (rute
 *       SELALU KIRI, delay SELALU 0, satu-satunya cara ganti itu
 *       edit-build-deploy)
 *    b. Jalankan metode 2 SEBELUM tekan PLAY, coba pilih rute PARKIR
 *       dengan delay 5 detik pakai dpad. Nggak ada build ulang sama
 *       sekali
 *
 *  PERCOBAAN 2 — Buktikan edge detection perlu
 *    a. Di metode2_MenuInit(), ganti kondisi
 *       "if (dpadKananSekarang && !dpadKananSebelumnya)" jadi cuma
 *       "if (dpadKananSekarang)" (hapus pengecekan tepinya)
 *    b. Coba TAHAN dpad kanan sebentar aja waktu menu jalan. Rute-nya
 *       lompat-lompat nggak terkendali?
 *    c. Kembalikan kodenya
 *
 *  PERCOBAAN 3 — Simulasikan hari pertandingan
 *    a. Minta temanmu SECARA MENDADAK bilang "kamu di sisi kanan,
 *       delay 3 detik!" tanpa kasih tahu sebelumnya
 *    b. Ukur berapa detik kamu butuh buat menyesuaikan lewat menu
 *       metode 2, dibandingkan seandainya harus edit kode
 *
 *  TANTANGAN
 *    a. Tambah pilihan menu ketiga: tekan gamepad1.a buat toggle
 *       antara "Aliansi MERAH" dan "Aliansi BIRU", yang membalik
 *       arah rute KIRI/KANAN (karena sisi lapangan mirror tergantung
 *       aliansi) — pola gabungan pilihan seperti ini yang dipakai
 *       kebanyakan tim FTC kompetitif di menu auto mereka
 * ============================================================================
 */
