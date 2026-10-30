package com.vinograd.zivinice.podaci

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Most između iOS obavijesti i navigacije. Swift javi ekran iz polja "ekran",
 * a App ga otvori čim je spreman (radi i kad je aplikacija bila zatvorena).
 */
object PushEkran {
    private val _ekran = MutableStateFlow<String?>(null)
    val ekran: StateFlow<String?> = _ekran

    /** Poziva AppDelegate.swift na dodir obavijesti. */
    fun otvori(ekran: String) {
        _ekran.value = ekran
    }

    /** App je otvorio ekran. */
    fun obradjeno() {
        _ekran.value = null
    }
}
