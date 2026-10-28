package com.vinograd.zivinice.podaci

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Praćenje čvora u bazi uživo — zamjena za Android addValueEventListener.
 * Firebase REST stream (text/event-stream): na svaki "put"/"patch" događaj čvor se ponovo učita.
 * Ako veza pukne (mreža, aplikacija u pozadini), ponovo se spaja nakon 5 s.
 */
internal object Uzivo {
    // Bez HttpTimeout: veza ostaje otvorena (Firebase šalje keep-alive svakih ~30 s)
    private val http: HttpClient by lazy { HttpClient { expectSuccess = false } }

    /** Radi dok traje korutina koja ga pozove. */
    suspend fun prati(putanja: String, ucitaj: suspend () -> Unit) {
        while (true) {
            ucitaj() // odmah (i nakon svakog prekida), kao prvi poziv listenera
            try {
                http.prepareGet("${Baza.URL}/$putanja.json") {
                    header(HttpHeaders.Accept, "text/event-stream")
                }.execute { r ->
                    if (!r.status.isSuccess()) return@execute
                    val kanal = r.bodyAsChannel()
                    var dogadjaj = ""
                    while (true) {
                        val red = kanal.readUTF8Line() ?: break
                        when {
                            red.startsWith("event:") -> dogadjaj = red.removePrefix("event:").trim()
                            red.startsWith("data:") -> when (dogadjaj) {
                                "put", "patch" -> ucitaj()
                                "cancel", "auth_revoked" -> return@execute
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // nema interneta / prekid — ponovni pokušaj ispod
            }
            delay(5_000)
        }
    }
}
