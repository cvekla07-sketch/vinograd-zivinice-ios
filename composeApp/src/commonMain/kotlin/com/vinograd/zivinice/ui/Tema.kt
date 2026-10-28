package com.vinograd.zivinice.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.vinograd.zivinice.resources.Res
import com.vinograd.zivinice.resources.poppins_medium
import org.jetbrains.compose.resources.Font

/** Tema 1:1 kao Android Theme.MyApplication (Material3 Light, bez tamne varijante). */
private val ShemaBoja = lightColorScheme(
    primary = Boje.ZelenaTamna,
    onPrimary = Boje.Bijela,
    primaryContainer = Boje.ZelenaPastel,
    onPrimaryContainer = Boje.ZelenaTamna,
    secondary = Boje.LjubicastaSrednja,
    onSecondary = Boje.Bijela,
    secondaryContainer = Boje.LjubicastaPastel,
    onSecondaryContainer = Boje.LjubicastaTamna,
    background = Boje.Krem,
    onBackground = Boje.TekstGlavni,
    surface = Boje.Bijela,
    onSurface = Boje.TekstGlavni,
)

/** Poppins Medium (jedini font u Android projektu). */
@Composable
fun Poppins(): FontFamily = FontFamily(Font(Res.font.poppins_medium, FontWeight.Medium))

@Composable
fun VinogradTema(sadrzaj: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShemaBoja,
        typography = Typography(),
        content = sadrzaj,
    )
}
