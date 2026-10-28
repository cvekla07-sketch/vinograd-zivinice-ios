package com.vinograd.zivinice

import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Slike sorti po ključu (zamjena za Android getIdentifier(kljuc, "drawable")).
 * Ključ = ime fajla u composeResources/drawable (isti kao na Androidu).
 * Sorta bez slike → null → prikazuje se ZamjenskaSlika.
 */
object SlikeSorti {
    private val mapa: Map<String, DrawableResource> = mapOf(
        "alinka" to Res.drawable.alinka,
        "almira" to Res.drawable.almira,
        "alvika" to Res.drawable.alvika,
        "arkadia" to Res.drawable.arkadia,
        "aromatika" to Res.drawable.aromatika,
        "avtografkalugina" to Res.drawable.avtografkalugina,
        "azija" to Res.drawable.azija,
        "bajkonur" to Res.drawable.bajkonur,
        "bananas" to Res.drawable.bananas,
        "basanti" to Res.drawable.basanti,
        "bijelalejdi" to Res.drawable.bijelalejdi,
        "bilanadija" to Res.drawable.bilanadija,
        "bogatjanovski" to Res.drawable.bogatjanovski,
        "bogema" to Res.drawable.bogema,
        "bravisimo" to Res.drawable.bravisimo,
        "bronislav" to Res.drawable.bronislav,
        "bruno" to Res.drawable.bruno,
        "buba" to Res.drawable.buba,
        "crnikristal" to Res.drawable.crnikristal,
        "damskakrasunja" to Res.drawable.damskakrasunja,
        "damskiugodnik" to Res.drawable.damskiugodnik,
        "dikson" to Res.drawable.dikson,
        "dnjeprovskisuvenir" to Res.drawable.dnjeprovskisuvenir,
        "dzoker" to Res.drawable.dzoker,
        "elina" to Res.drawable.elina,
        "emir" to Res.drawable.emir,
        "energodar" to Res.drawable.energodar,
        "etjud" to Res.drawable.etjud,
        "everest" to Res.drawable.everest,
        "glamur" to Res.drawable.glamur,
        "grafinija" to Res.drawable.grafinija,
        "halloween" to Res.drawable.halloween,
        "hilton" to Res.drawable.hilton,
        "huseinkelinbarmak" to Res.drawable.huseinkelinbarmak,
        "ilona" to Res.drawable.ilona,
        "imperatricja" to Res.drawable.imperatricja,
        "itum10" to Res.drawable.itum10,
        "itum9" to Res.drawable.itum9,
        "jubilejnovocerkaska" to Res.drawable.jubilejnovocerkaska,
        "julian" to Res.drawable.julian,
        "kameleon" to Res.drawable.kameleon,
        "karnaval" to Res.drawable.karnaval,
        "karolinakalugina" to Res.drawable.karolinakalugina,
        "kataleja" to Res.drawable.kataleja,
        "katalonia" to Res.drawable.katalonia,
        "kazanova" to Res.drawable.kazanova,
        "kazimir" to Res.drawable.kazimir,
        "kismisalisaizzemljecuda" to Res.drawable.kismisalisaizzemljecuda,
        "kismisararat" to Res.drawable.kismisararat,
        "kismisavatar" to Res.drawable.kismisavatar,
        "kismisbratvavilona" to Res.drawable.kismisbratvavilona,
        "kismiscimus" to Res.drawable.kismiscimus,
        "kismiscornobaivka" to Res.drawable.kismiscornobaivka,
        "kismisgeliodor" to Res.drawable.kismisgeliodor,
        "kismisjumbo" to Res.drawable.kismisjumbo,
        "kismislamborghini" to Res.drawable.kismislamborghini,
        "kismislandkruzer" to Res.drawable.kismislandkruzer,
        "kismislucistij" to Res.drawable.kismislucistij,
        "kismismarmeladnij" to Res.drawable.kismismarmeladnij,
        "kismispandora" to Res.drawable.kismispandora,
        "kismisratatuj" to Res.drawable.kismisratatuj,
        "kismissamuraj" to Res.drawable.kismissamuraj,
        "kismissarlota" to Res.drawable.kismissarlota,
        "kismissolomija" to Res.drawable.kismissolomija,
        "kismisveles" to Res.drawable.kismisveles,
        "kodrianka" to Res.drawable.kodrianka,
        "krasava" to Res.drawable.krasava,
        "ksenija" to Res.drawable.ksenija,
        "kumir" to Res.drawable.kumir,
        "kvartet" to Res.drawable.kvartet,
        "landis" to Res.drawable.landis,
        "maljcikspaljcik" to Res.drawable.maljcikspaljcik,
        "marmeladka" to Res.drawable.marmeladka,
        "masa" to Res.drawable.masa,
        "moldavia" to Res.drawable.moldavia,
        "monblan" to Res.drawable.monblan,
        "mustang" to Res.drawable.mustang,
        "pamjatibronislava" to Res.drawable.pamjatibronislava,
        "pervenecrani" to Res.drawable.pervenecrani,
        "podarokenergodaru" to Res.drawable.podarokenergodaru,
        "podaroknesvetaja" to Res.drawable.podaroknesvetaja,
        "polonez50" to Res.drawable.polonez50,
        "poltavskimanjukur" to Res.drawable.poltavskimanjukur,
        "pompei" to Res.drawable.pompei,
        "poslanik" to Res.drawable.poslanik,
        "preobrazenje" to Res.drawable.preobrazenje,
        "rafinad" to Res.drawable.rafinad,
        "rambo" to Res.drawable.rambo,
        "romansa" to Res.drawable.romansa,
        "rombik" to Res.drawable.rombik,
        "rubinovjubilej" to Res.drawable.rubinovjubilej,
        "rumejka" to Res.drawable.rumejka,
        "ruskorano" to Res.drawable.ruskorano,
        "sarada" to Res.drawable.sarada,
        "scelkuncik" to Res.drawable.scelkuncik,
        "sfinks" to Res.drawable.sfinks,
        "sjecanjenaucitelja" to Res.drawable.sjecanjenaucitelja,
        "slavaukrajine" to Res.drawable.slavaukrajine,
        "standart" to Res.drawable.standart,
        "stoletie" to Res.drawable.stoletie,
        "striz" to Res.drawable.striz,
        "sugra48" to Res.drawable.sugra48,
        "sugra53" to Res.drawable.sugra53,
        "sweetcelebration" to Res.drawable.sweetcelebration,
        "tajson" to Res.drawable.tajson,
        "taldun" to Res.drawable.taldun,
        "urum" to Res.drawable.urum,
        "vatra" to Res.drawable.vatra,
        "veljur" to Res.drawable.veljur,
        "velora" to Res.drawable.velora,
        "viktor" to Res.drawable.viktor,
        "vzljot" to Res.drawable.vzljot,
        "zolotijtelec" to Res.drawable.zolotijtelec,
    )

    fun za(kljuc: String): DrawableResource? = mapa[kljuc]
}

/** Opis sorte iz composeResources/files/opisi/{kljuc}.txt; null ako ne postoji ili je prazan. */
@OptIn(ExperimentalResourceApi::class)
suspend fun opisSorte(kljuc: String): String? = try {
    Res.readBytes("files/opisi/$kljuc.txt").decodeToString()
        .replace("\uFEFF", "")
        .replace("\r\n", "\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
        .ifEmpty { null }
} catch (e: Exception) {
    null
}
