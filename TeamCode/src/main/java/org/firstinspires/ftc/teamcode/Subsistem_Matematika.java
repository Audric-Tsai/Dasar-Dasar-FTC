package org.firstinspires.ftc.teamcode;

/**
 * ============================================================================
 *   SUBSISTEM MATEMATIKA (PURE MATH) — Dites di Auto20_UnitTesting
 * ============================================================================
 *
 *   Baca Auto20_UnitTesting DULU (di TeamCode/src/test/java/.../teamcode/)
 *   buat penjelasan lengkap kenapa file ini ada dan kenapa dia dites
 *   BEDA CARANYA dari semua file Auto0N lain.
 *
 *   Ringkasnya: rumus-rumus di bawah ini SUDAH ada di seri ini —
 *   bedaSudut (Auto02/04/05/07/08/19), kinematika mecanum (Auto13),
 *   konversi tick<->inci (Auto01), dan posisi/kecepatan profil
 *   trapesium (Auto17). Di file aslinya, rumus-rumus ini private,
 *   nempel di dalam class OpMode, dan CUMA bisa "dites" dengan cara
 *   nyalain robot beneran dan lihat apa dia gerak benar.
 *
 *   Di sini, rumus yang SAMA dipisah jadi method PUBLIC STATIC —
 *   nggak nyentuh hardware SAMA SEKALI, cuma angka masuk, angka
 *   keluar. Itu yang bikin dia BISA dites otomatis tanpa robot sama
 *   sekali (lihat Auto20_UnitTesting).
 * ============================================================================
 */
public class Subsistem_Matematika {

    /** Selisih dua sudut DERAJAT, dibereskan ke antara -180 dan 180. Sama seperti bedakanSudut() di Auto02/04/05/07/08/19. */
    public static double bedakanSudutDerajat(double target, double sekarang) {
        double selisih = target - sekarang;
        while (selisih >  180) selisih -= 360;
        while (selisih < -180) selisih += 360;
        return selisih;
    }

    /** Selisih dua sudut RADIAN, dibereskan ke antara -pi dan pi. Sama seperti bedaSudutRadian() di Auto07/08/19. */
    public static double bedakanSudutRadian(double target, double sekarang) {
        double selisih = target - sekarang;
        while (selisih >  Math.PI) selisih -= 2 * Math.PI;
        while (selisih < -Math.PI) selisih += 2 * Math.PI;
        return selisih;
    }

    /** Ubah jarak (inci) jadi jumlah tick encoder. Sama seperti inciKeTick() di Auto01 dst. */
    public static int inciKeTick(double inci, double diameterRodaInci, double tickPerPutaran) {
        double kelilingRoda = Math.PI * diameterRodaInci;
        return (int) ((inci / kelilingRoda) * tickPerPutaran);
    }

    /** Ubah tick encoder jadi jarak (inci). Kebalikan dari inciKeTick(). */
    public static double tickKeInci(int tick, double diameterRodaInci, double tickPerPutaran) {
        double kelilingRoda = Math.PI * diameterRodaInci;
        return (tick / tickPerPutaran) * kelilingRoda;
    }

    /**
     * Kinematika mecanum, PERSIS rumus di Auto13_KinematikaMecanum
     * dan sample resmi FTC SDK (BasicOmniOpMode_Linear.java).
     * Mengembalikan {depanKiri, depanKanan, belakangKiri, belakangKanan},
     * sudah dinormalisasi supaya nggak ada yang melebihi 1.0.
     */
    public static double[] hitungPowerMecanum(double axial, double lateral, double putar) {

        double depanKiri     = axial + lateral + putar;
        double depanKanan    = axial - lateral - putar;
        double belakangKiri  = axial - lateral + putar;
        double belakangKanan = axial + lateral - putar;

        double maksimum = Math.max(1.0, Math.max(
                Math.max(Math.abs(depanKiri), Math.abs(depanKanan)),
                Math.max(Math.abs(belakangKiri), Math.abs(belakangKanan))));

        return new double[]{
                depanKiri / maksimum,
                depanKanan / maksimum,
                belakangKiri / maksimum,
                belakangKanan / maksimum
        };
    }

    // ========================================================================
    //   PROFIL TRAPESIUM — persis Auto17_ProfilGerak
    // ========================================================================

    public static double waktuPercepatanProfil(double jarakTotal, double kecepatanJelajah, double percepatan) {
        double waktuPercepatanPenuh = kecepatanJelajah / percepatan;
        double jarakPercepatanPenuh = 0.5 * percepatan * waktuPercepatanPenuh * waktuPercepatanPenuh;

        if (2 * jarakPercepatanPenuh > jarakTotal) {
            double kecepatanPuncak = Math.sqrt(percepatan * jarakTotal);
            return kecepatanPuncak / percepatan;
        }
        return waktuPercepatanPenuh;
    }

    public static double waktuTotalProfil(double jarakTotal, double kecepatanJelajah, double percepatan) {
        double waktuPercepatan = waktuPercepatanProfil(jarakTotal, kecepatanJelajah, percepatan);
        double jarakPercepatan = 0.5 * percepatan * waktuPercepatan * waktuPercepatan;
        double kecepatanPuncak = percepatan * waktuPercepatan;
        double jarakJelajah = jarakTotal - 2 * jarakPercepatan;
        double waktuJelajah = jarakJelajah > 0 ? jarakJelajah / kecepatanPuncak : 0;
        return 2 * waktuPercepatan + waktuJelajah;
    }

    public static double kecepatanProfil(double t, double jarakTotal, double kecepatanJelajah, double percepatan) {
        double waktuPercepatan = waktuPercepatanProfil(jarakTotal, kecepatanJelajah, percepatan);
        double waktuTotal = waktuTotalProfil(jarakTotal, kecepatanJelajah, percepatan);

        if (t < waktuPercepatan) {
            return percepatan * t;
        } else if (t < waktuTotal - waktuPercepatan) {
            return percepatan * waktuPercepatan;
        } else if (t < waktuTotal) {
            double tSisa = waktuTotal - t;
            return percepatan * tSisa;
        }
        return 0;
    }

    public static double posisiProfil(double t, double jarakTotal, double kecepatanJelajah, double percepatan) {
        double waktuPercepatan = waktuPercepatanProfil(jarakTotal, kecepatanJelajah, percepatan);
        double waktuTotal = waktuTotalProfil(jarakTotal, kecepatanJelajah, percepatan);

        if (t < waktuPercepatan) {
            return 0.5 * percepatan * t * t;
        } else if (t < waktuTotal - waktuPercepatan) {
            double jarakPercepatan = 0.5 * percepatan * waktuPercepatan * waktuPercepatan;
            double kecepatanPuncak = percepatan * waktuPercepatan;
            return jarakPercepatan + kecepatanPuncak * (t - waktuPercepatan);
        } else if (t < waktuTotal) {
            double tSisa = waktuTotal - t;
            return jarakTotal - 0.5 * percepatan * tSisa * tSisa;
        }
        return jarakTotal;
    }
}
