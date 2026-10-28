package com.vinograd.zivinice

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController

/** Ulazna tačka koju poziva Swift (ContentView.swift). */
fun MainViewController() = ComposeUIViewController(
    configure = {
        // Ekran se ne pomjera sam kad se otvori tastatura — dugmad na dnu idu iznad tastature
        // preko WindowInsets.ime (kao Android: max(systemBars.bottom, ime.bottom)).
        onFocusBehavior = OnFocusBehavior.DoNothing
    }
) { App() }
