package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * ============================================================================
 *   AUTO PROFIL GERAK — Merencanakan Gerakan SEBELUM Menjalankannya
 * ============================================================================
 *
 *   Auto01 metode 4 (dan hampir semua "jalan lurus" sejak itu) selalu
 *   fokus ke MASALAH BERHENTI: power berkurang mendekati target biar
 *   nggak nge-rem mendadak. Tapi coba perhatikan ujung yang SATUNYA
 *   LAGI — awal gerakan.
 *
 *   Di metode proporsional (power = KP x errorJarak), waktu robot
 *   MULAI dari diam, errorJarak-nya PALING BESAR — jadi power yang
 *   dikirim LANGSUNG melompat ke nilai TERBESAR sejak loop PERTAMA.
 *   Robot nyentak dari diam ke hampir-power-penuh dalam SATU langkah.
 *   Itu jolt/hentakan juga, cuma di awal, bukan di akhir — dan nggak
 *   ada satu pun file sebelum ini yang membahasnya.
 *
 *   PROFIL GERAK (MOTION PROFILE) menyelesaikan dua-duanya sekaligus
 *   dengan cara yang beda total: daripada menghitung power dari
 *   ERROR (kayak semua PID sejauh ini), kita RENCANAKAN dulu seperti
 *   apa seharusnya KECEPATAN robot di SETIAP DETIK perjalanan —
 *   SEBELUM robotnya mulai gerak sama sekali — lalu robot cuma
 *   MENGIKUTI rencana itu.
 *
 *   BENTUK RENCANANYA — TRAPESIUM:
 *
 *     kecepatan
 *         |      ______________
 *         |     /              \
 *         |    /                \
 *         |   /                  \
 *         |__/____________________\____ waktu
 *         0  percepatan  jelajah  perlambatan
 *
 *     - PERCEPATAN: kecepatan naik LINEAR dari 0 (nggak ada hentakan)
 *     - JELAJAH: kecepatan konstan di puncak (secepat mungkin, aman)
 *     - PERLAMBATAN: kecepatan turun LINEAR ke 0 (nggak ada rem mendadak)
 *
 *   Kalau jaraknya terlalu PENDEK buat sempat capai kecepatan jelajah,
 *   bentuknya jadi SEGITIGA (langsung dari percepatan ke perlambatan,
 *   tanpa fase jelajah) — kode di bawah menangani kasus ini juga.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive -> sama Auto01, gerak lurus saja
 *     (nggak ada belok di file ini — fokusnya di BENTUK kecepatan,
 *     bukan arah)
 *
 *   LANJUT KE: Auto18_LintasanMulus — konsep "kecepatan jelajah
 *   tetap, nggak usah melambat" dari metode 2/3 di file ini dipakai
 *   lagi di sana, buat titik ANTARA di sebuah lintasan (yang PUNYA
 *   arah/belokan, beda dari file ini yang sengaja lurus doang).
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Profil Gerak (Belajar)", group = "Belajar")
public class Auto17_ProfilGerak extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    private static final double TICK_PER_PUTARAN   = 560.0;
    private static final double DIAMETER_RODA_INCI = 3.54;

    private static final double POWER_MINIMUM = 0.15;
    private static final double TOLERANSI_INCI = 0.5;

    /**
     * KECEPATAN JELAJAH & PERCEPATAN — dalam INCI/DETIK dan
     * INCI/DETIK^2, bukan cuma angka power 0-1 seperti file lain.
     *
     * Ini SATU-SATUNYA file di seri ini yang butuh kecepatan MAKSIMAL
     * ROBOT dalam satuan fisik asli (inci/detik), karena profil butuh
     * tahu berapa detik idealnya perjalanan ini makan waktu.
     *
     * CARA UKUR KECEPATAN_MAKS_FISIK_INCI_DETIK:
     *   Jalankan robot power 1.0 lurus selama 2 detik, ukur jaraknya
     *   pakai meteran, bagi dengan 2. Itu kecepatan maksimal robotmu.
     */
    private static final double KECEPATAN_MAKS_FISIK_INCI_DETIK = 30.0; // UKUR SENDIRI

    /** Kecepatan jelajah SENGAJA di bawah maksimal fisik, nyisain "headroom" buat koreksi di metode 3. */
    private static final double KECEPATAN_JELAJAH_INCI_DETIK = 20.0;

    /** Seberapa cepat kecepatan naik/turun. Lebih besar = ramp lebih tajam (lebih dekat ke sentakan). */
    private static final double PERCEPATAN_INCI_DETIK2 = 40.0;

    /** Dipakai metode 3 — seberapa kuat koreksi posisi ditambahkan di atas feedforward. */
    private static final double KP_KOREKSI = 0.05;

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

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_ProporsionalLama(48);
        //metode2_ProfilTrapesium(48);
        //metode3_ProfilDenganKoreksi(48);
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(3000);
    }

    // ========================================================================
    //   METODE 1 — PROPORSIONAL LAMA (REVIEW, DENGAN CACAT BARU TERLIHAT)
    // ========================================================================
    /**
     * Ini persis Auto01 metode 4. Perhatikan LOOP PERTAMA: posisi
     * masih 0, jadi errorJarak = jarakInci PENUH (paling besar yang
     * mungkin), dan power = KP x errorJarak juga langsung besar sejak
     * detik pertama — nggak ada ramp-up sama sekali. Lihat telemetry
     * "Power" di beberapa loop pertama, bandingkan dengan metode 2.
     */
    private void metode1_ProporsionalLama(double jarakInci) {

        int targetTick = inciKeTick(jarakInci);
        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        double kecepatanMaksPower = KECEPATAN_JELAJAH_INCI_DETIK / KECEPATAN_MAKS_FISIK_INCI_DETIK;

        while (opModeIsActive() && posisiRataRata() < targetTick) {
            int sisa = targetTick - posisiRataRata();
            double power = Range.clip(kecepatanMaksPower * ((double) sisa / targetTick), POWER_MINIMUM, kecepatanMaksPower);
            motorKiri.setPower(power);
            motorKanan.setPower(power);

            telemetry.addData("METODE", "1 — Proporsional Lama");
            telemetry.addData("Power", "%.3f", power);
            telemetry.addLine("Lihat loop PERTAMA — power-nya udah besar dari awal.");
            telemetry.update();
        }
        berhenti();
    }

    // ========================================================================
    //   METODE 2 — PROFIL TRAPESIUM (FEEDFORWARD MURNI)
    // ========================================================================
    /**
     * IDENYA:
     *   Hitung DULU seluruh bentuk rencana (kapan mulai jelajah,
     *   kapan mulai melambat, berapa lama totalnya) SEBELUM gerak.
     *   Lalu tiap loop, tanya "menurut rencana, DETIK INI harusnya
     *   kecepatan berapa?" — convert ke power, kirim ke motor. TITIK.
     *
     *   Ini namanya FEEDFORWARD: power dihitung dari RENCANA/WAKTU,
     *   BUKAN dari error/posisi aktual. Robot nggak pernah "melihat"
     *   encoder buat memutuskan power-nya di metode ini — dia cuma
     *   percaya jadwal yang sudah dihitung di awal.
     *
     * KENAPA INI BISA JADI MASALAH:
     *   Kalau baterai lemah, atau karpetnya lebih kesat dari
     *   biasanya, kecepatan ASLI robot nggak akan persis sama dengan
     *   yang direncanakan — tapi feedforward murni TETAP PERCAYA
     *   rencananya, nggak pernah mengoreksi diri. Robot bisa berhenti
     *   duluan sebelum sampai target, atau lewat dikit. Metode 3
     *   membenahi ini.
     */
    private void metode2_ProfilTrapesium(double jarakInci) {

        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        double waktuTotal = waktuTotalProfil(jarakInci);
        ElapsedTime waktu = new ElapsedTime();

        while (opModeIsActive() && waktu.seconds() < waktuTotal) {

            double t = waktu.seconds();
            double kecepatanRencana = kecepatanProfil(t, jarakInci);
            double power = Range.clip(kecepatanRencana / KECEPATAN_MAKS_FISIK_INCI_DETIK, 0, 1.0);

            motorKiri.setPower(power);
            motorKanan.setPower(power);

            telemetry.addData("METODE", "2 — Profil Trapesium (feedforward murni)");
            telemetry.addData("Waktu", "%.2f / %.2f detik", t, waktuTotal);
            telemetry.addData("Kecepatan rencana", "%.1f inci/detik", kecepatanRencana);
            telemetry.addData("Power", "%.3f", power);
            telemetry.addData("Posisi aktual (encoder)", "%.1f inci", tickKeInci(posisiRataRata()));
            telemetry.update();
        }
        berhenti();
    }

    // ========================================================================
    //   METODE 3 — PROFIL + KOREKSI (FEEDFORWARD + FEEDBACK)
    // ========================================================================
    /**
     * TAMBAHAN DARI METODE 2:
     *   Selain feedforward (dari rencana), tambahkan JUGA koreksi
     *   kecil berdasarkan SELISIH antara posisi RENCANA dan posisi
     *   ASLI (dari encoder):
     *
     *     errorPosisi = posisiRencana - posisiAktual
     *     koreksi = KP_KOREKSI x errorPosisi
     *     power = feedforward + koreksi
     *
     *   Kalau robot PERSIS sesuai rencana, errorPosisi = 0, koreksi
     *   = 0, power = feedforward MURNI (sama seperti metode 2).
     *   Koreksi cuma "muncul" waktu ada PENYIMPANGAN dari rencana —
     *   itu sebabnya KP_KOREKSI bisa kecil (dia cuma tugas nge-
     *   patch beda kecil, bukan menggerakkan robot dari nol).
     *
     * INI POLA YANG SAMA DIPAKAI ROBOT PROFESIONAL:
     *   feedforward (dari MODEL/rencana) menangani SEBAGIAN BESAR
     *   pekerjaan, feedback/PID (dari SENSOR) cuma membetulkan sisa
     *   kesalahan kecil. Auto19_PIDF nanti kasih nama resmi buat pola ini
     *   — "F" di PIDF — dan menerapkannya di konteks yang beda
     *   (lengan melawan gravitasi, bukan jarak lurus).
     *
     * BERHENTI PAKAI ENCODER, BUKAN CUMA WAKTU:
     *   Beda dari metode 2 (berhenti kalau waktu profil habis),
     *   metode ini berhenti kalau POSISI ASLI sudah sampai target —
     *   lebih jujur, karena robotnya beneran mengecek dia sudah
     *   sampai atau belum.
     */
    private void metode3_ProfilDenganKoreksi(double jarakInci) {

        motorKiri.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorKiri.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorKanan.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        double waktuTotal = waktuTotalProfil(jarakInci);
        ElapsedTime waktu = new ElapsedTime();

        while (opModeIsActive()) {

            double posisiAktual = tickKeInci(posisiRataRata());
            if (waktu.seconds() >= waktuTotal && Math.abs(jarakInci - posisiAktual) <= TOLERANSI_INCI) break;

            double t = Math.min(waktu.seconds(), waktuTotal);
            double posisiRencana = posisiProfil(t, jarakInci);
            double kecepatanRencana = kecepatanProfil(t, jarakInci);

            double feedforward = kecepatanRencana / KECEPATAN_MAKS_FISIK_INCI_DETIK;
            double errorPosisi = posisiRencana - posisiAktual;
            double koreksi = KP_KOREKSI * errorPosisi;

            double power = Range.clip(feedforward + koreksi, 0, 1.0);

            motorKiri.setPower(power);
            motorKanan.setPower(power);

            telemetry.addData("METODE", "3 — Profil + Koreksi");
            telemetry.addData("Posisi rencana", "%.1f inci", posisiRencana);
            telemetry.addData("Posisi aktual", "%.1f inci", posisiAktual);
            telemetry.addData("Feedforward", "%.3f", feedforward);
            telemetry.addData("Koreksi", "%.3f", koreksi);
            telemetry.update();
        }
        berhenti();
    }

    // ========================================================================
    //   MATEMATIKA PROFIL TRAPESIUM
    // ========================================================================
    /**
     * Tiga fungsi ini menghitung "menurut rencana, pada detik t,
     * robot harusnya di posisi mana / kecepatan berapa" — TANPA
     * peduli sama sekali di mana robotnya SEBENARNYA berada. Murni
     * matematika kinematika (v = a*t, x = 1/2*a*t^2), sama sekali
     * nggak menyentuh hardware.
     */

    private double waktuPercepatanProfil(double jarakTotal) {
        double waktuPercepatanPenuh = KECEPATAN_JELAJAH_INCI_DETIK / PERCEPATAN_INCI_DETIK2;
        double jarakPercepatanPenuh = 0.5 * PERCEPATAN_INCI_DETIK2 * waktuPercepatanPenuh * waktuPercepatanPenuh;

        if (2 * jarakPercepatanPenuh > jarakTotal) {
            // Segitiga: jarak kepencet sebelum sempat capai kecepatan jelajah.
            double kecepatanPuncak = Math.sqrt(PERCEPATAN_INCI_DETIK2 * jarakTotal);
            return kecepatanPuncak / PERCEPATAN_INCI_DETIK2;
        }
        return waktuPercepatanPenuh;
    }

    private double waktuTotalProfil(double jarakTotal) {
        double waktuPercepatan = waktuPercepatanProfil(jarakTotal);
        double jarakPercepatan = 0.5 * PERCEPATAN_INCI_DETIK2 * waktuPercepatan * waktuPercepatan;
        double kecepatanPuncak = PERCEPATAN_INCI_DETIK2 * waktuPercepatan;
        double jarakJelajah = jarakTotal - 2 * jarakPercepatan;
        double waktuJelajah = jarakJelajah > 0 ? jarakJelajah / kecepatanPuncak : 0;
        return 2 * waktuPercepatan + waktuJelajah;
    }

    private double kecepatanProfil(double t, double jarakTotal) {
        double waktuPercepatan = waktuPercepatanProfil(jarakTotal);
        double waktuTotal = waktuTotalProfil(jarakTotal);

        if (t < waktuPercepatan) {
            return PERCEPATAN_INCI_DETIK2 * t;
        } else if (t < waktuTotal - waktuPercepatan) {
            return PERCEPATAN_INCI_DETIK2 * waktuPercepatan;
        } else if (t < waktuTotal) {
            double tSisa = waktuTotal - t;
            return PERCEPATAN_INCI_DETIK2 * tSisa;
        }
        return 0;
    }

    private double posisiProfil(double t, double jarakTotal) {
        double waktuPercepatan = waktuPercepatanProfil(jarakTotal);
        double waktuTotal = waktuTotalProfil(jarakTotal);

        if (t < waktuPercepatan) {
            return 0.5 * PERCEPATAN_INCI_DETIK2 * t * t;
        } else if (t < waktuTotal - waktuPercepatan) {
            double jarakPercepatan = 0.5 * PERCEPATAN_INCI_DETIK2 * waktuPercepatan * waktuPercepatan;
            double kecepatanPuncak = PERCEPATAN_INCI_DETIK2 * waktuPercepatan;
            return jarakPercepatan + kecepatanPuncak * (t - waktuPercepatan);
        } else if (t < waktuTotal) {
            double tSisa = waktuTotal - t;
            return jarakTotal - 0.5 * PERCEPATAN_INCI_DETIK2 * tSisa * tSisa;
        }
        return jarakTotal;
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

    private double tickKeInci(int tick) {
        return (tick / TICK_PER_PUTARAN) * kelilingRoda();
    }

    private int posisiRataRata() {
        return (Math.abs(motorKiri.getCurrentPosition()) + Math.abs(motorKanan.getCurrentPosition())) / 2;
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
 *  PERCOBAAN 1 — Lihat hentakan awal metode 1
 *    a. Jalankan metode 1, perhatikan "Power" di 2-3 baris telemetry
 *       PERTAMA setelah start
 *    b. Jalankan metode 2, bandingkan — power-nya mulai dari berapa?
 *
 *  PERCOBAAN 2 — Bandingkan waktu tempuh
 *    a. Ukur waktu total metode 1 sampai selesai (pakai stopwatch HP)
 *    b. Ukur metode 2. Lebih cepat? Kenapa (petunjuk: metode 2 bisa
 *       jalan di KECEPATAN_JELAJAH penuh lebih lama)?
 *
 *  PERCOBAAN 3 — Buktikan metode 2 "buta"
 *    a. Kosongkan baterai sampai agak lemah (atau tahan robot pelan
 *       pakai tangan sebentar di tengah jalan buat mensimulasikan
 *       gesekan ekstra)
 *    b. Jalankan metode 2, lihat "Posisi aktual" di akhir — apa dia
 *       beneran sampai jarakInci, atau meleset?
 *    c. Ulangi pakai metode 3 — apa dia lebih dekat ke target?
 *
 *  PERCOBAAN 4 — Uji kasus segitiga
 *    a. Jalankan metode 2 dengan jarak PENDEK (misalnya 6 inci —
 *       lebih pendek dari jarak buat capai kecepatan jelajah penuh)
 *    b. Perhatikan "Kecepatan rencana" — apa dia sempat menyentuh
 *       KECEPATAN_JELAJAH_INCI_DETIK sama sekali, atau langsung turun
 *       lagi setelah puncak yang lebih rendah?
 *
 *  TANTANGAN
 *    a. Terapkan pola profil+koreksi yang sama ke belokPID() dari
 *       Auto05 — rencanakan dulu profil KECEPATAN SUDUT (bukan
 *       jarak), baru gerakkan motor sesuai rencana + koreksi IMU
 * ============================================================================
 */
