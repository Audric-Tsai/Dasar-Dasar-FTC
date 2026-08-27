package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * ============================================================================
 *   AUTO ARSITEKTUR SUBSISTEM — Kenapa Kode Auto01-14 Selalu "Menyalin Ulang"
 * ============================================================================
 *
 *   Kalau kamu baca ulang Auto01 sampai Auto14, ada sesuatu yang
 *   berulang di HAMPIR SEMUA file: kelilingRoda(), inciKeTick(),
 *   posisiRataRata(), berhenti() — fungsi-fungsi kecil yang SAMA,
 *   ditulis ULANG dari nol di tiap file. Itu SENGAJA sepanjang seri
 *   ini — supaya tiap pelajaran berdiri sendiri, bisa dibaca terpisah
 *   tanpa perlu buka file lain buat ngerti konteksnya.
 *
 *   Tapi bayangkan kamu robot kompetisi SUNGGUHAN, dan nemu BUG di
 *   rumus inciKeTick() (misalnya salah kali/bagi). Kalau itu disalin
 *   di 12+ file berbeda, kamu harus INGAT dan PERBAIKI di 12+ tempat.
 *   Lupa satu = bug itu tetap ada, sembunyi di file yang kelewat.
 *
 *   SOLUSINYA: SUBSISTEM (SUBSYSTEM). Pisahkan "cara menggerakkan
 *   drivetrain" dan "cara mengoperasikan capit" jadi CLASS TERSENDIRI
 *   (lihat Subsistem_Drivetrain.java dan Subsistem_Capit.java, satu
 *   folder dengan file ini) — ditulis SEKALI, dipakai ULANG oleh
 *   SEMUA OpMode yang butuh. Bug di satu tempat = perbaiki di satu
 *   tempat, otomatis kepakai di semua OpMode yang menggunakan class
 *   itu.
 *
 *   BANDINGKAN FILE INI DENGAN Auto12_SubsistemMekanisme:
 *   Auto12 itu SATU file ~400 baris, semua logic motor+servo+sensor
 *   nyampur jadi satu. File INI mengerjakan tugas yang MIRIP (gerak,
 *   capit), tapi karena Subsistem_Drivetrain dan Subsistem_Capit
 *   sudah "dibungkus" jadi subsistem terpisah, runOpMode() di bawah
 *   jadi SANGAT PENDEK dan GAMPANG DIBACA — hampir kayak baca resep,
 *   bukan baca matematika.
 *
 *   ROBOT INI PAKAI:
 *     left_drive, right_drive -> lewat Subsistem_Drivetrain.java
 *     servo_lengan             -> lewat Subsistem_Capit.java
 *
 *   LANJUT KE: Auto16_PilihRute — satu masalah praktis terakhir:
 *   pola "PILIH METODE DI SINI" yang dipakai di SEMUA file Auto0N
 *   (termasuk file ini) masih butuh build ulang buat ganti perilaku.
 *   Auto16 membenahi itu buat hari pertandingan.
 *
 * ============================================================================
 */

@Autonomous(name = "Auto Arsitektur Subsistem (Belajar)", group = "Belajar")
public class Auto15_ArsitekturSubsistem extends LinearOpMode {

    @Override
    public void runOpMode() {

        // Bandingkan Bagian 2+3 di file-file sebelumnya (belasan baris
        // hardwareMap.get()/setDirection()/setZeroPowerBehavior() per
        // subsistem) dengan DUA BARIS ini. Semua detail setup itu
        // sekarang hidup di dalam konstruktor Subsistem_Drivetrain dan Subsistem_Capit.
        Subsistem_Drivetrain drivetrain = new Subsistem_Drivetrain(hardwareMap, this);
        Subsistem_Capit capit = new Subsistem_Capit(hardwareMap);

        capit.tutup(); // posisi awal yang aman buat dibawa ke lapangan

        telemetry.addLine("SIAP — tekan PLAY untuk mulai");
        telemetry.update();

        waitForStart();

        // Ini "resep"-nya. Baca ulang Auto03_Gabungan buat bandingkan —
        // isinya SAMA (bentuk kotak), tapi di sana jalanLurus()/belok()
        // adalah fungsi PRIVATE di dalam OpMode itu sendiri. Di sini,
        // mereka MILIK objek drivetrain, dipakai lewat "titik" (.) —
        // itu bedanya kode yang jadi SUBSISTEM vs kode yang numpang di
        // satu file OpMode.
        drivetrain.jalanLurus(24);
        capit.buka();
        drivetrain.belokWaktu(-0.3, 0.3, 1.0); // belok kiri ~90 derajat, sederhana
        drivetrain.jalanLurus(12);
        capit.tutup();

        telemetry.addLine("SELESAI");
        telemetry.update();
        sleep(3000);
    }
}

/*
 * ============================================================================
 *   LATIHAN — kerjakan berurutan
 * ============================================================================
 *
 *  PERCOBAAN 1 — Baca alurnya
 *    a. Baca runOpMode() di file ini SEBELUM baca
 *       Subsistem_Drivetrain.java
 *    b. Bisa nebak apa yang dilakukan drivetrain.jalanLurus(24) tanpa
 *       buka Subsistem_Drivetrain.java sama sekali, cuma dari
 *       NAMANYA? Ini manfaat lain dari subsistem: nama method yang
 *       jelas bikin OpMode gampang dibaca TANPA harus tahu detail
 *       implementasi.
 *
 *  PERCOBAAN 2 — Sengaja bikin bug lalu perbaiki
 *    a. Di Subsistem_Drivetrain.java, ubah DIAMETER_RODA_INCI jadi
 *       angka yang salah (misal dikali 2)
 *    b. Perhatikan: HANYA file Subsistem_Drivetrain.java yang perlu
 *       diubah. Auto15 nggak perlu disentuh sama sekali, dan efeknya
 *       otomatis kepakai di SEMUA OpMode lain yang nanti kamu buat
 *       memakai class Subsistem_Drivetrain ini
 *    c. Kembalikan ke angka semula
 *
 *  PERCOBAAN 3 — Tambah subsistem baru
 *    a. Buat file SensorJarak.java baru (class biasa, bukan OpMode)
 *       yang membungkus DistanceSensor dari Auto10, dengan method
 *       semacam sudahDekat(double batasInci)
 *    b. Pakai class itu di Auto15 buat mengganti drivetrain.jalanLurus(24)
 *       yang pertama, supaya berhenti berdasar sensor, bukan jarak tetap
 *
 *  TANTANGAN
 *    a. Upgrade Subsistem_Drivetrain.jalanLurus() dan tambah
 *       Subsistem_Drivetrain.belokPID() supaya pakai IMU + PID penuh
 *       (dari Auto04/05), bukan versi sederhana di atas.
 *       Subsistem_Drivetrain butuh nyimpan referensi IMU juga di
 *       konstruktornya.
 *    b. Setelah itu, SEMUA OpMode yang pakai class Subsistem_Drivetrain
 *       otomatis jadi lebih presisi tanpa perlu diubah satu-satu —
 *       itulah inti kenapa arsitektur ini berharga di robot yang
 *       kodenya makin lama makin besar.
 * ============================================================================
 */
