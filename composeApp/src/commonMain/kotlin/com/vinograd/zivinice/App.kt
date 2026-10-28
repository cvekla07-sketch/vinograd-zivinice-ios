package com.vinograd.zivinice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.vinograd.zivinice.ekrani.CjenovnikEkran
import com.vinograd.zivinice.ekrani.DetaljiEkran
import com.vinograd.zivinice.ekrani.DetaljiTretmanaEkran
import com.vinograd.zivinice.ekrani.DnevnikEkran
import com.vinograd.zivinice.ekrani.HomeEkran
import com.vinograd.zivinice.ekrani.KatalogEkran
import com.vinograd.zivinice.ekrani.KontaktEkran
import com.vinograd.zivinice.ekrani.KontaktRezervacijeEkran
import com.vinograd.zivinice.ekrani.KorpaEkran
import com.vinograd.zivinice.ekrani.NarudzbaEkran
import com.vinograd.zivinice.ekrani.RezervacijaEkran
import com.vinograd.zivinice.ekrani.SlikaPrekoEkran
import com.vinograd.zivinice.ekrani.TretmaniEkran
import com.vinograd.zivinice.ekrani.VodicEkran
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

    // Stanje ekrana u stacku (rememberSaveable) ostaje dok je ekran u stacku — kao Activity u pozadini.
    // Kad se ekran skine sa stacka, njegovo stanje se briše (novo otvaranje kreće od nule).
    val cuvar = rememberSaveableStateHolder()
    val velicina = nav.stack.size
    var najveca by remember { mutableIntStateOf(1) }
    LaunchedEffect(velicina) {
        for (i in velicina until najveca) cuvar.removeState(i)
        najveca = velicina
    }

    VinogradTema {
        cuvar.SaveableStateProvider(nav.stack.lastIndex) {
        when (val e = nav.trenutni) {
            Ekran.Home -> HomeEkran(nav)
            Ekran.Katalog -> KatalogEkran(nav)
            is Ekran.Detalji -> DetaljiEkran(e.naziv, nav)
            is Ekran.SlikaPreko -> SlikaPrekoEkran(e.naziv, nav)
            Ekran.Korpa -> KorpaEkran(nav)
            Ekran.Narudzba -> NarudzbaEkran(nav)
            Ekran.Cjenovnik -> CjenovnikEkran(nav)
            Ekran.Rezervacija -> RezervacijaEkran(nav)
            is Ekran.KontaktRezervacije -> KontaktRezervacijeEkran(e.stavke, nav)
            Ekran.Tretmani -> TretmaniEkran(nav)
            is Ekran.DetaljiTretmana -> DetaljiTretmanaEkran(e.mjesecKey, nav)
            Ekran.Vodic -> VodicEkran(nav)
            Ekran.Dnevnik -> DnevnikEkran(nav)
            Ekran.Kontakt -> KontaktEkran(nav)
        }
        }
    }
}
