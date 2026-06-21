package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.LotomaniaDraw
import com.example.stats.GeneratorMode
import com.example.stats.NumberStats
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorTab(
    stats: List<NumberStats>,
    lastDraw: LotomaniaDraw?,
    generatedGames: List<List<Int>>,
    lastUsedMode: GeneratorMode,
    onGenerateGames: (GeneratorMode, Int, List<NumberStats>, LotomaniaDraw?) -> Unit,
    onClearGames: () -> Unit
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(GeneratorMode.RANDOM) }
    var selectedQuantity by remember { mutableStateOf(5) }
    var showModeSelector by remember { mutableStateOf(false) }

    val quantities = listOf(1, 5, 10, 20, 50, 100)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Gerador Inteligente de Jogos",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Formule dezenas otimizadas para Lotomania de acordo com diretrizes matemáticas e algoritmos avançados.",
                color = SoftGrayText,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // 1. Escolha do Algoritmo / Modo de Geração
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Algoritmo de Otimização",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF040608), RoundedCornerShape(8.dp))
                            .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                            .clickable { showModeSelector = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(selectedMode.label, color = NeonBlueAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(selectedMode.desc, color = SoftGrayText, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp), maxLines = 1)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }

        // 2. Escolha da Quantidade de Jogos
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Quantidade de Bilhetes",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quantities) { q ->
                            val isSelected = selectedQuantity == q
                            Box(
                                modifier = Modifier
                                    .size(width = 52.dp, height = 36.dp)
                                    .background(
                                        if (isSelected) NeonPurpleAccent.copy(alpha = 0.2f) else Color(0xFF040608),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonPurpleAccent else BorderSlate,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedQuantity = q }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$q",
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Botões de Ações de Geração
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onGenerateGames(selectedMode, selectedQuantity, stats, lastDraw)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlueAccent, contentColor = Color.Black),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Gerar Bilhetes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (generatedGames.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearGames,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BloombergRed),
                        border = BorderStroke(1.dp, BloombergRed.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Limpar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 4. Exibição dos Jogos Gerados
        if (generatedGames.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${generatedGames.size} Bilhetes Otimizados (${lastUsedMode.label})",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val formattedStr = generatedGames.mapIndexed { idx, game ->
                                "Jogo ${idx + 1}: " + game.joinToString(",") { String.format("%02d", it) }
                            }.joinToString("\n")
                            val clip = ClipData.newPlainText("Bilhetes Lotomania AI PRO", formattedStr)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copiar Todos", fontSize = 12.sp)
                    }
                }
            }

            items(generatedGames.size) { index ->
                val game = generatedGames[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSlate, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "BILHETE #${index + 1}",
                                color = NeonPurpleAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Row {
                                Icon(
                                    Icons.Default.Casino,
                                    contentDescription = null,
                                    tint = SoftGrayText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("50 Números", color = SoftGrayText, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Renderiza o grid compacto de dezenas dentro do bilhete
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            // Imprime números separados por hífen em parágrafo responsivo
                            Text(
                                text = game.joinToString(" • ") { String.format("%02d", it) },
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 5. Cláusula legal e avisos
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = BloombergRed.copy(alpha = 0.11f)),
                border = BorderStroke(0.5.dp, BloombergRed.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = BloombergRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Aviso Legal Sobre Jogos",
                            color = BloombergRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            stringResource(id = com.example.R.string.disclaimer_prediction),
                            color = SoftGrayText,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Justify,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Modal de seleção / dropdown do algoritmo
    if (showModeSelector) {
        val modesList = GeneratorMode.values()
        AlertDialog(
            onDismissRequest = { showModeSelector = false },
            title = { Text("Algoritmos de Otimização", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(modesList) { mode ->
                        val isSelected = selectedMode == mode
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMode = mode
                                    showModeSelector = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) NeonPurpleAccent.copy(alpha = 0.2f) else Color(0xFF131722)
                            ),
                            border = BorderStroke(if (isSelected) 1.dp else 0.5.dp, if (isSelected) NeonPurpleAccent else BorderSlate),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(mode.label, color = if (isSelected) NeonBlueAccent else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(mode.desc, color = SoftGrayText, fontSize = 10.sp, lineHeight = 14.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showModeSelector = false }) {
                    Text("Cancelar", color = NeonBlueAccent)
                }
            },
            containerColor = DarkSlateCard
        )
    }
}
