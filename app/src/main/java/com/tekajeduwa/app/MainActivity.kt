package com.tekajeduwa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Bg = Color(0xFF0F172A)
val Card = Color(0xFF111C31)
val Border = Color(0xFF334155)
val Indigo = Color(0xFF6366F1)
val IndigoDark = Color(0xFF4F46E5)
val Green = Color(0xFF059669)
val Red = Color(0xFFB91C1C)
val Slate = Color(0xFF94A3B8)
val TextMain = Color(0xFFE2E8F0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("tekajeduwa", MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(background = Bg)) {
                App(prefs)
            }
        }
    }
}

@Composable
fun App(prefs: android.content.SharedPreferences) {
    var seats by remember { mutableStateOf(loadSeats(prefs)) }
    var period by remember { mutableStateOf(prefs.getInt("period", 0)) }
    var editTarget by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showBulk by remember { mutableStateOf(false) }

    fun simpan(s: Seats, p: Int = period) {
        seats = s
        period = p
        saveSeats(prefs, s, p)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Tekajeduwa", color = Indigo, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatBox(period.toString(), "Periode")
                StatBox(seats.flatten().filterNotNull().size.toString(), "Siswa")
            }
        }

        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16223A), RoundedCornerShape(10.dp))
                .border(1.dp, Border, RoundedCornerShape(10.dp))
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "PAPAN TULIS / DEPAN KELAS",
                color = Slate,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
            )
        }
        Spacer(Modifier.height(12.dp))

        // label arah tiap barisan
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(30.dp))
            COLUMN_SIZES.indices.forEach { c ->
                val ganjil = (c + 1) % 2 == 1
                Text(
                    "B${c + 1} ${if (ganjil) "↗" else "↖"}",
                    color = if (ganjil) Color(0xFFA5B4FC) else Color(0xFF6EE7B7),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // grid kursi: baris = kedalaman meja, kolom = barisan
        for (d in 0 until MAX_DEPTH) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${d + 1}",
                    color = Slate,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(30.dp),
                )
                seats.forEachIndexed { c, col ->
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 3.dp)) {
                        if (d < col.size) {
                            SeatCell(
                                siswa = col[d],
                                onClick = { editTarget = c to d },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { simpan(rollSeats(seats), period + 1) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoDark),
                modifier = Modifier.weight(2f),
            ) { Text("▶ Rolling", color = Color.White) }
            Button(
                onClick = { simpan(shuffleSeats(seats)) },
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                modifier = Modifier.weight(2f),
            ) { Text("🎲 Acak", color = Color.White) }
            Button(
                onClick = { simpan(emptySeats(), 0) },
                colors = ButtonDefaults.buttonColors(containerColor = Red),
                modifier = Modifier.weight(1f),
            ) { Text("⟲", color = Color.White) }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { showBulk = true },
            colors = ButtonDefaults.buttonColors(containerColor = Card),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("+ Isi Nama Cepat", color = TextMain) }

        Spacer(Modifier.height(16.dp))
        Text(
            "Baris ganjil nyerong kanan, genap nyerong kiri. Paling depan pindah ke paling belakang.",
            color = Slate,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }

    // dialog edit satu kursi
    editTarget?.let { (c, d) ->
        var draft by remember(c to d) { mutableStateOf(seats[c][d]?.nama ?: "") }
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("Barisan ${c + 1} · Meja ${d + 1}") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Nama siswa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = {
                    val nama = draft.trim()
                    val baru = seats.mapIndexed { ci, col ->
                        col.mapIndexed { di, s ->
                            when {
                                ci == c && di == d && nama.isNotEmpty() -> Siswa(newId(), nama)
                                ci == c && di == d -> null
                                else -> s
                            }
                        }
                    }
                    simpan(baru)
                    editTarget = null
                }) { Text("Simpan") }
            },
            dismissButton = {
                Row {
                    Button(onClick = {
                        simpan(
                            seats.mapIndexed { ci, col ->
                                col.mapIndexed { di, s -> if (ci == c && di == d) null else s }
                            },
                        )
                        editTarget = null
                    }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("Hapus") }
                    Spacer(Modifier.width(6.dp))
                    Button(onClick = { editTarget = null }, colors = ButtonDefaults.buttonColors(containerColor = Card)) { Text("Batal") }
                }
            },
            containerColor = Card,
            textContentColor = TextMain,
            titleContentColor = TextMain,
        )
    }

    // dialog isi nama cepat
    if (showBulk) {
        BulkDialog(
            onDismiss = { showBulk = false },
            onApply = { text, ganti ->
                simpan(fillFromText(seats, text, ganti))
                showBulk = false
            },
        )
    }
}

@Composable
fun StatBox(angka: String, ket: String) {
    Column(
        modifier = Modifier
            .background(Card, RoundedCornerShape(10.dp))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(angka, color = Color(0xFFA5B4FC), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(ket, color = Slate, fontSize = 10.sp)
    }
}

@Composable
fun SeatCell(siswa: Siswa?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .then(
                if (siswa != null) {
                    Modifier
                        .background(Color(0xFF1E1B4B), RoundedCornerShape(9.dp))
                        .border(1.5.dp, Indigo, RoundedCornerShape(9.dp))
                } else {
                    Modifier
                        .border(1.5.dp, Border, RoundedCornerShape(9.dp))
                },
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (siswa != null) {
            Text(
                siswa.nama,
                color = TextMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        } else {
            Text("+", color = Color(0xFF475569), fontSize = 18.sp)
        }
    }
}

@Composable
fun BulkDialog(onDismiss: () -> Unit, onApply: (String, Boolean) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var gantiSemua by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Isi Nama Cepat") },
        text = {
            Column {
                Text(
                    "Satu nama per baris, mengisi dari depan ke belakang.",
                    color = Slate,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Andi\nBudi\nCitra") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !gantiSemua, onClick = { gantiSemua = false })
                    Text("Isi kosong saja", color = TextMain, fontSize = 13.sp)
                    Spacer(Modifier.width(12.dp))
                    RadioButton(selected = gantiSemua, onClick = { gantiSemua = true })
                    Text("Ganti semua", color = TextMain, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(text, gantiSemua) }) { Text("Terapkan") }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Card)) {
                Text("Batal")
            }
        },
        containerColor = Card,
        textContentColor = TextMain,
        titleContentColor = TextMain,
    )
}
