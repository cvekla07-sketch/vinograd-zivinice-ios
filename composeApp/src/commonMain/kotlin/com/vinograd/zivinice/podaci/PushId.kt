package com.vinograd.zivinice.podaci

import com.vinograd.zivinice.platform.sadaMs
import kotlin.random.Random

/**
 * Firebase push ID — isti algoritam kao DatabaseReference.push().getKey() na Androidu
 * (8 znakova vremena + 12 nasumičnih; hronološki sortirani).
 */
internal object PushId {
    private const val ZNAKOVI = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz"
    private var zadnjeVrijeme = 0L
    private val zadnjiNasumicni = IntArray(12)

    fun novi(): String {
        var sada = sadaMs()
        val duplikat = sada == zadnjeVrijeme
        zadnjeVrijeme = sada

        val vrijeme = CharArray(8)
        for (i in 7 downTo 0) {
            vrijeme[i] = ZNAKOVI[(sada % 64).toInt()]
            sada /= 64
        }
        if (!duplikat) {
            for (i in 0 until 12) zadnjiNasumicni[i] = Random.nextInt(64)
        } else {
            var i = 11
            while (i >= 0 && zadnjiNasumicni[i] == 63) {
                zadnjiNasumicni[i] = 0
                i--
            }
            if (i >= 0) zadnjiNasumicni[i]++
        }
        val sb = StringBuilder().append(vrijeme)
        for (n in zadnjiNasumicni) sb.append(ZNAKOVI[n])
        return sb.toString()
    }
}
