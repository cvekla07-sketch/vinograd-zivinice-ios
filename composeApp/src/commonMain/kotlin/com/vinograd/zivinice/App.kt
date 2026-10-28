package com.vinograd.zivinice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.vinograd.zivinice.ekrani.HomeEkran
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Sezona
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.ui.VinogradTema

/** Glavna aplikacija — kao MainActivity: učita cjenovnik, sorte i sezonu, pa prikazuje ekrane. */
@Composable
fun App() {
    val nav = remember { Navigator() }

    LaunchedEffect(Unit) {
        Sorte.osvjezi()
        Cjenovnik.osvjezi()
        Sezona.osvjezi()
    }

    VinogradTema {
        when (val e = nav.trenutni) {
            Ekran.Home -> HomeEkran(nav)
            Ekran.Katalog -> UIzradi("Katalog", nav)
            is Ekran.Detalji -> UIzradi(e.naziv, nav)
            is Ekran.SlikaPreko -> UIzradi(e.naziv, nav)
            Ekran.Korpa -> UIzradi("Korpa", nav)
            Ekran.Narudzba -> UIzradi("Narudžba", nav)
            Ekran.Cjenovnik -> UIzradi("Cjenovnik", nav)
            Ekran.Rezervacija -> UIzradi("Rezervacija", nav)
            Ekran.KontaktRezervacije -> UIzradi("Rezervacija", nav)
            Ekran.Tretmani -> UIzradi("Tretmani", nav)
            is Ekran.DetaljiTretmana -> UIzradi("Tretman", nav)
            Ekran.Vodic -> UIzradi("Vodič", nav)
            Ekran.Dnevnik -> UIzradi("Dnevnik", nav)
            Ekran.Kontakt -> UIzradi("Kontakt", nav)
        }
    }
}
