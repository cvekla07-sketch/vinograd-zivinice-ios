package com.vinograd.zivinice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.vinograd.zivinice.ekrani.CjenovnikEkran
import com.vinograd.zivinice.ekrani.DetaljiEkran
import com.vinograd.zivinice.ekrani.HomeEkran
import com.vinograd.zivinice.ekrani.KatalogEkran
import com.vinograd.zivinice.ekrani.KorpaEkran
import com.vinograd.zivinice.ekrani.NarudzbaEkran
import com.vinograd.zivinice.ekrani.SlikaPrekoEkran
import com.vinograd.zivinice.podaci.Cjenovnik
import com.vinograd.zivinice.podaci.Sezona
import com.vinograd.zivinice.podaci.Sorte
import com.vinograd.zivinice.ui.VinogradTema

/** Glavna aplikacija — kao MainActivity: učita cjenovnik, sorte i sezonu, pa prikazuje ekrane. */
@Composable
fun App() {
    val nav = remember { Navigator() }

    // Coil: slike sorti iz baze (slikaUrl) preko Ktor-a
    setSingletonImageLoaderFactory { ctx ->
        ImageLoader.Builder(ctx)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    LaunchedEffect(Unit) {
        Sorte.osvjezi()
        Cjenovnik.osvjezi()
        Sezona.osvjezi()
    }

    VinogradTema {
        when (val e = nav.trenutni) {
            Ekran.Home -> HomeEkran(nav)
            Ekran.Katalog -> KatalogEkran(nav)
            is Ekran.Detalji -> DetaljiEkran(e.naziv, nav)
            is Ekran.SlikaPreko -> SlikaPrekoEkran(e.naziv, nav)
            Ekran.Korpa -> KorpaEkran(nav)
            Ekran.Narudzba -> NarudzbaEkran(nav)
            Ekran.Cjenovnik -> CjenovnikEkran(nav)
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
