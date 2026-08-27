package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * ============================================================================
 *   AUTO SENSOR JARAK — Berhenti Berdasarkan Apa yang DILIHAT, Bukan Ditebak
 * ============================================================================
 *
 *   Auto01 ngajarin jalan sejauh N inci pakai encoder. Itu jalan
 *   bagus SELAMA posisi awal robot di lapangan SELALU SAMA PERSIS
 *   tiap match — karena N inci itu angka TETAP yang kamu hitung dari
 *   satu titik start yang diasumsikan konsisten.
 *
 *   Kenyataannya: robot ditaruh manusia di lapangan, dan manusia
 *   nggak pernah taruh benda PERSIS di titik yang sama tiap kali.
 *   Meleset 2-3 cm dari posisi seharusnya itu NORMAL. Encoder nggak
 *   peduli itu — dia tetap jalan N inci PERSIS dari mana pun dia
 *   mulai, jadi hasil akhirnya ikut meleset 2-3 cm juga.
 *
 *   Sensor jarak (distance sensor, kerja pakai Time-of-Flight — dia
 *   nembak cahaya inframerah, ukur berapa lama pantulannya balik)
 *   menyelesaikan ini dengan cara BEDA TOTAL: robot nggak peduli dia
 *   MULAI dari mana. Dia cuma peduli "SEKARANG jaraknya ke tembok
 *   berapa", dan itu selalu benar nggak peduli posisi awalnya
 *   meleset berapa pun — sensor ngukur KEADAAN SEKARANG, bukan
 *   MENGHITUNG dari titik awal kayak encoder.
 *
 *   KETERBATASAN YANG PERLU DIINGAT:
 *     - Jangkauan terbatas (biasanya sampai ~2 meter, tergantung
 *       sensornya, dan makin nggak akurat waktu mendekati batas itu)
 *     - Cuma "melihat" SATU titik lurus di depannya (kerucut sempit),
 *       bukan area luas — kalau tembok didepannya nggak rata/miring,
 *       bisa salah baca
 *     - Permukaan gelap/menyerap cahaya atau memantulkan cahaya ke
 *       arah lain (kaca, permukaan mengkilap miring) bisa bikin
 *       bacaan salah atau nggak terbaca sama sekali
 *     - Bacaannya BERISIK (noisy) — angka bisa naik-turun kecil
 *       walau jaraknya nggak berubah sama sekali. Ini dibahas di
 *       metode 4.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive -> sama Auto01
 *     sensor_jarak             -> distance sensor, menghadap ke DEPAN
 *
 *   LANJUT KE: Auto11_SensorWarna — masalah yang sama (bacaan sensor
 *   dipengaruhi kondisi di luar objeknya sendiri) muncul lagi, kali
 *   ini di sensor warna, dengan solusi yang beda bentuknya.
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Sensor Jarak (Belajar)", group = "Belajar")
public class Auto10_SensorJarak extends LinearOpMode {

    // ========================================================================
    //   BAGIAN 1 — ANGKA-ANGKA PENTING
    // ========================================================================

    /** Berhenti kira-kira sejauh ini dari benda di depan. */
    private static final double JARAK_BERHENTI_INCI = 6.0;

    private static final double KECEPATAN = 0.3;
    private static final double POWER_MINIMUM = 0.15;
    private static final double KP_JARAK_SENSOR = 0.05;
    private static final double TOLERANSI_INCI = 0.5;

    /**
     * UKURAN JENDELA FILTER (dipakai metode 4)
     *
     * Berapa banyak bacaan terakhir yang dirata-rata. Lebih besar =
     * lebih halus TAPI lebih lambat bereaksi ke perubahan asli.
     * Lebih kecil = lebih cepat bereaksi TAPI kurang efektif meredam
     * noise. Ini SELALU tarik-ulur, bukan "makin besar makin bagus".
     */
    private static final int UKURAN_JENDELA_FILTER = 5;

    // ========================================================================
    //   BAGIAN 2 — DAFTAR HARDWARE
    // ========================================================================

    private DcMotor motorKiri;
    private DcMotor motorKanan;
    private DistanceSensor sensorJarak;

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

        sensorJarak = hardwareMap.get(DistanceSensor.class, "sensor_jarak");

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.addLine("Taruh sesuatu (tembok/kotak) di depan robot.");
        telemetry.update();

        waitForStart();

        // ====================================================================
        //
        //   PILIH METODE DI SINI — aktifkan SATU baris saja.
        //
        // ====================================================================
        metode1_BacaJarak();
        //metode2_MajuSampaiJarak();
        //metode3_ProporsionalKeJarak();
        //metode4_JarakDifilter();
        // ====================================================================

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(3000);
    }

    // ========================================================================
    //   METODE 1 — BACA JARAK MENTAH
    // ========================================================================
    /**
     * IDENYA:
     *   Cuma tampilkan angka dari sensor, terus-menerus. Belum ada
     *   gerakan sama sekali.
     *
     * KENAPA MULAI DI SINI:
     *   Coba taruh tanganmu deket sensor, jauhkan pelan-pelan, lihat
     *   angkanya berubah. Coba juga taruh benda GELAP vs benda
     *   TERANG di jarak yang SAMA — apa angkanya konsisten?
     *
     *   Kalau sensor nggak mendeteksi apa pun (nggak ada benda dalam
     *   jangkauan), dia melapor jarak TAK TERHINGGA (infinity).
     *   Selalu cek ini sebelum mempercayai angkanya — kode di bawah
     *   nunjukkin caranya.
     */
    private void metode1_BacaJarak() {
        while (opModeIsActive()) {

            double jarak = sensorJarak.getDistance(DistanceUnit.INCH);

            telemetry.addData("METODE", "1 — Baca Jarak Mentah");
            if (Double.isInfinite(jarak)) {
                telemetry.addData("Jarak", "TAK TERBACA (nggak ada benda dalam jangkauan)");
            } else {
                telemetry.addData("Jarak", "%.2f inci", jarak);
            }
            telemetry.addLine();
            telemetry.addLine("Gerakkan benda maju-mundur di depan sensor.");
            telemetry.update();
        }
    }

    // ========================================================================
    //   METODE 2 — MAJU SAMPAI JARAK (BANG-BANG)
    // ========================================================================
    /**
     * IDENYA:
     *   Full power sampai sensor bilang "sudah cukup dekat"
     *   (jarak <= JARAK_BERHENTI_INCI), lalu berhenti total.
     *
     * KENAPA INI TERASA KAYAK DÉJÀ VU:
     *   Ini bang-bang, pola yang SAMA seperti Auto02 metode 3 (IMU
     *   bang-bang) — cuma sekarang sensornya beda (jarak, bukan
     *   sudut). Cacatnya juga SAMA: MOMENTUM. Robot yang jalan
     *   kencang nggak langsung berhenti begitu power = 0, dia
     *   meluncur dulu — bisa saja nabrak benda di depannya walau
     *   kodenya "sudah" bilang berhenti waktu masih di jarak aman.
     *
     * BUKTIKAN SENDIRI:
     *   Naikkan KECEPATAN, lihat seberapa dekat/jauh dia berhenti
     *   dari target JARAK_BERHENTI_INCI yang seharusnya.
     */
    private void metode2_MajuSampaiJarak() {

        while (opModeIsActive() && sensorJarak.getDistance(DistanceUnit.INCH) > JARAK_BERHENTI_INCI) {
            motorKiri.setPower(KECEPATAN);
            motorKanan.setPower(KECEPATAN);

            telemetry.addData("METODE", "2 — Bang-Bang");
            telemetry.addData("Jarak", "%.2f inci", sensorJarak.getDistance(DistanceUnit.INCH));
            telemetry.addLine("Full power sampai mentok, baru berhenti.");
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   METODE 3 — PROPORSIONAL KE JARAK
    // ========================================================================
    /**
     * TAMBAHAN DARI METODE 2:
     *   Sama seperti Auto01 metode 4, power berkurang mendekati
     *   target — bedanya di sini "seberapa dekat ke target" dibaca
     *   LANGSUNG dari sensor tiap loop, bukan dihitung dari encoder.
     *
     *     errorJarak = jarakSekarang - JARAK_BERHENTI_INCI
     *     power = KP_JARAK_SENSOR x errorJarak
     *
     *   Makin dekat ke JARAK_BERHENTI_INCI, makin kecil errorJarak,
     *   makin kecil juga power-nya — robot melambat sendiri sebelum
     *   sampai, bukan nge-rem mendadak.
     */
    private void metode3_ProporsionalKeJarak() {

        while (opModeIsActive()) {

            double jarakSekarang = sensorJarak.getDistance(DistanceUnit.INCH);
            double errorJarak = jarakSekarang - JARAK_BERHENTI_INCI;
            if (errorJarak <= TOLERANSI_INCI) break;

            double power = Range.clip(KP_JARAK_SENSOR * errorJarak, POWER_MINIMUM, KECEPATAN);
            motorKiri.setPower(power);
            motorKanan.setPower(power);

            telemetry.addData("METODE", "3 — Proporsional");
            telemetry.addData("Jarak", "%.2f inci", jarakSekarang);
            telemetry.addData("Power", "%.3f", power);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   METODE 4 — JARAK DIFILTER (MOVING AVERAGE)
    // ========================================================================
    /**
     * MASALAH DI METODE 3:
     *   Perhatikan baik-baik angka "Jarak" di metode 1 waktu benda
     *   di depannya DIAM SAJA (nggak digerakkan). Angkanya nggak
     *   pernah PERSIS sama tiap pembacaan — naik-turun dikit, biasa
     *   disebut NOISE. Kalau errorJarak di metode 3 pas-pasan deket
     *   TOLERANSI_INCI waktu noise sedang membuatnya lompat naik-
     *   turun, robot bisa "gemetar" — berhenti, jalan dikit, berhenti
     *   lagi — padahal jarak aslinya udah nggak berubah.
     *
     * SOLUSINYA — MOVING AVERAGE:
     *   Simpan beberapa bacaan TERAKHIR (bukan cuma satu), lalu
     *   pakai RATA-RATANYA sebagai "jarak yang dipercaya", bukan
     *   bacaan mentah paling baru. Noise cenderung naik-turun di
     *   SEKITAR nilai asli — waktu dirata-rata, naik-turunnya saling
     *   meniadakan, dan yang tersisa mendekati nilai aslinya.
     *
     * KENAPA INI TARIK-ULUR (TRADE-OFF), BUKAN SOLUSI GRATIS:
     *   Rata-rata dari 5 bacaan TERAKHIR berarti bacaan itu
     *   "mewakili" keadaan BEBERAPA LOOP YANG LALU, bukan keadaan
     *   PERSIS saat ini. Kalau target berubah CEPAT (misalnya sensor
     *   tiba-tiba lihat tembok baru), filter butuh beberapa loop buat
     *   "mengejar" ke angka yang benar — ini disebut LAG. Jendela
     *   filter lebih besar = lebih halus TAPI lebih lambat bereaksi.
     *   Bandingkan "Mentah" vs "Difilter" di telemetry buat lihat
     *   bedanya secara langsung.
     */
    private void metode4_JarakDifilter() {

        double[] jendela = new double[UKURAN_JENDELA_FILTER];
        int isiJendela = 0; // berapa slot yang sudah kepakai (awal OpMode belum penuh)
        int indeksBerikutnya = 0;

        while (opModeIsActive()) {

            double jarakMentah = sensorJarak.getDistance(DistanceUnit.INCH);
            if (Double.isInfinite(jarakMentah)) jarakMentah = 200; // batas aman kalau nggak ada benda

            jendela[indeksBerikutnya] = jarakMentah;
            indeksBerikutnya = (indeksBerikutnya + 1) % UKURAN_JENDELA_FILTER;
            if (isiJendela < UKURAN_JENDELA_FILTER) isiJendela++;

            double jumlah = 0;
            for (int i = 0; i < isiJendela; i++) jumlah += jendela[i];
            double jarakDifilter = jumlah / isiJendela;

            double errorJarak = jarakDifilter - JARAK_BERHENTI_INCI;
            if (errorJarak <= TOLERANSI_INCI) break;

            double power = Range.clip(KP_JARAK_SENSOR * errorJarak, POWER_MINIMUM, KECEPATAN);
            motorKiri.setPower(power);
            motorKanan.setPower(power);

            telemetry.addData("METODE", "4 — Jarak Difilter");
            telemetry.addData("Mentah", "%.2f inci", jarakMentah);
            telemetry.addData("Difilter", "%.2f inci", jarakDifilter);
            telemetry.addData("Power", "%.3f", power);
            telemetry.update();
        }

        berhenti();
    }

    // ========================================================================
    //   FUNGSI PEMBANTU
    // ========================================================================

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
 *  PERCOBAAN 1 — Lihat noise-nya beneran
 *    a. Jalankan metode 1, taruh benda diam di depan sensor
 *    b. Perhatikan angkanya selama 10 detik TANPA menggerakkan apa
 *       pun. Seberapa besar naik-turunnya?
 *
 *  PERCOBAAN 2 — Bandingkan permukaan
 *    a. Ulangi metode 1 dengan benda GELAP (kain hitam) di jarak
 *       tertentu, catat bacaannya
 *    b. Ganti dengan benda TERANG di jarak yang SAMA PERSIS, catat
 *       lagi. Beda?
 *
 *  PERCOBAAN 3 — Rasakan overshoot bang-bang
 *    a. Jalankan metode 2 dengan KECEPATAN dinaikkan ke 0.6
 *    b. Ukur jarak akhir robot ke bendanya pakai penggaris. Seberapa
 *       jauh dari JARAK_BERHENTI_INCI yang diminta?
 *
 *  PERCOBAAN 4 — Rasakan trade-off filter
 *    a. Jalankan metode 4 dengan UKURAN_JENDELA_FILTER = 2
 *    b. Ganti jadi 20, jalankan lagi
 *    c. Gerakkan benda MENJAUH tiba-tiba selagi robot jalan di kedua
 *       percobaan itu — mana yang lebih lambat "sadar" jaraknya
 *       berubah?
 *
 *  TANTANGAN
 *    a. Gabungkan sensor jarak dengan IMU (Auto02): robot maju
 *       sampai sensor jarak bilang cukup dekat, SAMBIL menjaga arah
 *       tetap lurus pakai koreksi IMU proporsional (persis seperti
 *       Auto03 jalanLurus, tapi kondisi berhentinya diganti jadi
 *       sensor jarak, bukan encoder).
 * ============================================================================
 */
