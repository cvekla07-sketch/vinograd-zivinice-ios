package com.vinograd.zivinice.podaci

/**
 * Slanje narudžbi i rezervacija — isto kao Android: push ID → setValue na narudzbe/{id} / rezervacije/{id}.
 * Pozivaju ga SAMO ekrani Narudžba i Rezervacija (Dio 4).
 */
object Slanje {
    /** Novi ID (kao push().getKey()) — ekran ga stavlja u Narudzba/Rezervacija prije slanja. */
    fun noviId(): String = PushId.novi()

    suspend fun posaljiNarudzbu(n: Narudzba): Boolean = Baza.upisi("narudzbe/${n.id}", n.uJson())

    suspend fun posaljiRezervaciju(r: Rezervacija): Boolean = Baza.upisi("rezervacije/${r.id}", r.uJson())
}
