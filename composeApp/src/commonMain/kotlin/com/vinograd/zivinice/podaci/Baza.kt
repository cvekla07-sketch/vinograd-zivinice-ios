package com.vinograd.zivinice.podaci

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Firebase Realtime Database preko REST-a (ista baza i ista pravila kao Android).
 * Kupac se ne prijavljuje — kao ni na Androidu.
 */
internal object Baza {
    const val URL = "https://vinograd-zivinice-default-rtdb.europe-west1.firebasedatabase.app"

    private val http: HttpClient by lazy {
        HttpClient {
            expectSuccess = false
            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 15_000
            }
        }
    }

    /** GET {putanja}.json → JSON ili null (nema interneta / greška / nema podataka). */
    suspend fun citaj(putanja: String): JsonElement? = try {
        val r = http.get("$URL/$putanja.json")
        if (!r.status.isSuccess()) null
        else Json.parseToJsonElement(r.bodyAsText()).takeIf { it !is JsonNull }
    } catch (e: Exception) {
        null
    }

    /** Kao citaj, ali razlikuje prazan čvor (JsonNull) od greške / nema interneta (null). */
    suspend fun citajIliPrazno(putanja: String): JsonElement? = try {
        val r = http.get("$URL/$putanja.json")
        if (!r.status.isSuccess()) null else Json.parseToJsonElement(r.bodyAsText())
    } catch (e: Exception) {
        null
    }

    /** PUT {putanja}.json — zamjenjuje čvor (kao setValue). */
    suspend fun upisi(putanja: String, tijelo: JsonElement): Boolean = try {
        http.put("$URL/$putanja.json") {
            contentType(ContentType.Application.Json)
            setBody(tijelo.toString())
        }.status.isSuccess()
    } catch (e: Exception) {
        false
    }

    /** PATCH {putanja}.json — dopunjava čvor (kao updateChildren). */
    suspend fun dopuni(putanja: String, tijelo: JsonObject): Boolean = try {
        http.patch("$URL/$putanja.json") {
            contentType(ContentType.Application.Json)
            setBody(tijelo.toString())
        }.status.isSuccess()
    } catch (e: Exception) {
        false
    }
}

// ---------- pomoćne funkcije za čitanje JSON-a ----------

internal fun JsonObject.tekst(kljuc: String): String? =
    (this[kljuc] as? JsonPrimitive)?.takeIf { it.isString }?.content

internal fun JsonObject.broj(kljuc: String): Double? =
    (this[kljuc] as? JsonPrimitive)?.takeIf { !it.isString }?.doubleOrNull

internal fun JsonObject.dugi(kljuc: String): Long? =
    (this[kljuc] as? JsonPrimitive)?.let { it.longOrNull ?: it.doubleOrNull?.toLong() }

internal fun JsonObject.istina(kljuc: String): Boolean? =
    (this[kljuc] as? JsonPrimitive)?.takeIf { !it.isString }?.booleanOrNull
