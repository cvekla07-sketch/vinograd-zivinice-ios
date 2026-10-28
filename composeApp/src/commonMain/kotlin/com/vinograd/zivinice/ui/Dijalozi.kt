package com.vinograd.zivinice.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Info dijalog — 1:1 LijepiDialog.info (dialog_info.xml):
 * zeleni header sa ikonom, krem tijelo, do 3 dugmeta (neutral / negativ / pozitiv).
 * Dugme sa null tekstom se ne prikazuje. Svaki klik zatvara dijalog pa poziva akciju.
 */
@Composable
fun InfoDijalog(
    ikona: String,
    naslov: String,
    sadrzaj: String,
    onZatvori: () -> Unit,
    pozitivTekst: String? = null,
    onPozitiv: (() -> Unit)? = null,
    neutralTekst: String? = null,
    onNeutral: (() -> Unit)? = null,
    negativTekst: String? = null,
    onNegativ: (() -> Unit)? = null,
) {
    Dialog(onDismissRequest = onZatvori, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFBF6EC))
        ) {
            // HEADER zeleni
            Row(
                Modifier.fillMaxWidth().background(Color(0xFF1F3D2E)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.padding(end = 10.dp).size(40.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center,
                ) { Text(ikona, fontSize = 18.sp) }
                Text(
                    naslov, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, fontFamily = Poppins(),
                )
            }
            // Tijelo
            Box(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                Text(
                    sadrzaj, fontSize = 14.sp, color = Color(0xFF3D4A40), fontFamily = Poppins(),
                    lineHeight = 22.sp, modifier = Modifier.padding(20.dp),
                )
            }
            // Dugmad
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
            ) {
                if (neutralTekst != null) {
                    OutlinedButton(
                        onClick = { onZatvori(); onNeutral?.invoke() },
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                    ) { DugmeTekst(neutralTekst, Color(0xFF1F3D2E)) }
                }
                if (negativTekst != null) {
                    TextButton(
                        onClick = { onZatvori(); onNegativ?.invoke() },
                        shape = RoundedCornerShape(100.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                    ) { DugmeTekst(negativTekst, Color(0xFF5C7268)) }
                }
                if (pozitivTekst != null) {
                    Button(
                        onClick = { onZatvori(); onPozitiv?.invoke() },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F3D2E)),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) { DugmeTekst(pozitivTekst, Color.White) }
                }
            }
        }
    }
}

@Composable
private fun DugmeTekst(t: String, boja: Color) {
    Text(t, color = boja, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins())
}
