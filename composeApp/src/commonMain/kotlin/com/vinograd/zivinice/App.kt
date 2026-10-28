package com.vinograd.zivinice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
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
import com.vinograd.zivinice.podaci.Stat
import com.vinograd.zivinice.podaci.Uredjaj
import com.vinograd.zivinice.podaci.Uzivo
import com.vinograd.zivinice.ui.VinogradTema
import kotlinx.coroutines.launch

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

    // Sorte, cjenovnik i sezona se prate uživo, kao Android addValueEventListener
    LaunchedEffect(Unit) {
        launch { Uzivo.prati("sorte") { Sorte.osvjezi() } }
        launch { Uzivo.prati("cjenovnik") { Cjenovnik.osvjezi() } }
        launch { Uzivo.prati("postavke/sezona") { Sezona.osvjezi() } }
        // Brojač posjeta + statistika uređaja (kao MainActivity.onCreate)
        Stat.posjeta()
        Uredjaj.zabiljezi()
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

    // Veliki tekst iz iOS postavki raste najviše do 1,15× — da raspored ne puca
    val gustina = LocalDensity.current
    val ogranicena = Density(gustina.density, gustina.fontScale.coerceAtMost(1.15f))

    CompositionLocalProvider(LocalDensity provides ogranicena) {
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
}
