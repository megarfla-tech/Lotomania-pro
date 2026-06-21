package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.LotomaniaDraw
import com.example.ui.theme.*

@Composable
fun ComparatorTab(draws: List<LotomaniaDraw>) {
    var comparisonRange by remember { mutableStateOf(2) } // Comparar: 2, 10, 100, 500 draws
    
    val rangeDraws = remember(draws, comparisonRange) {
        draws.take(comparisonRange)
    }

    // Cálculos de comparação
    val commonNumbers = remember(rangeDraws) {
        if (rangeDraws.isEmpty()) return@remember emptyList<Int>()
        var baseSet = rangeDraws.first().dezenas.toSet()
        for (i in 1 until rangeDraws.size) {
            baseSet = baseSet.intersect(rangeDraws[i].dezenas.toSet())
        }
        baseSet.sorted()
    }

    val unionNumbers = remember(rangeDraws) {
        val totalNumbers = mutableSetOf<Int>()
        rangeDraws.forEach { totalNumbers.addAll(it.dezenas) }
        totalNumbers.size
    }

    val duplicateFrequency = remember(rangeDraws) {
        val countMap = mutableMapOf<Int, Int>()
        rangeDraws.forEach { draw ->
            draw.dezenas.forEach { num ->
                countMap[num] = (countMap[num] ?: 0) + 1
            }
        }
        countMap.entries.sortedByDescending { it.value }.take(15)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Comparador Multiciclo",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Compare as coincidências de dezenas sorteadas entre dezenas de concursos passados em tempo real.",
                color = SoftGrayText,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // 1. Seleção de Range para Comparação
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Conjunto de Comparação",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val ranges = listOf(2, 10, 100, 500)
                        ranges.forEach { r ->
                            val isSelected = comparisonRange == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) NeonPurpleAccent.copy(alpha = 0.2f) else Color(0xFF040608),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonPurpleAccent else BorderSlate,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { comparisonRange = r }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val label = when(r) {
                                    2 -> "2 Ciclos"
                                    10 -> "10 Ciclos"
                                    100 -> "100 Ciclos"
                                    else -> "500 Ciclos"
                                }
                                Text(
                                    label,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Resultados da Análise de Comparação
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "MÉTRICAS COINCIDENTES",
                            color = NeonBlueAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Icon(
                            Icons.Default.Compare,
                            contentDescription = null,
                            tint = NeonPurpleAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        "Números de alta convergência inter-ciclo:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    if (comparisonRange == 2) {
                        if (commonNumbers.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    "Comuns de $comparisonRange sorteios: " + commonNumbers.joinToString(", ") { String.format("%02d", it) },
                                    color = BloombergGreen,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        } else {
                            Text("Nenhuma dezena incomum dividida entre estes 2 concursos.", color = Color.Gray, fontSize = 11.sp)
                        }
                    } else {
                        // Para ranges grandes, exibimos as ocorrências mais quentes ponderadas no período
                        Text(
                            "Os de maior convergência na amostra dos últimos $comparisonRange concursos:",
                            color = SoftGrayText,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            duplicateFrequency.take(5).forEach { entry ->
                                val frequencyPercentage = (entry.value.toDouble() / rangeDraws.size.toDouble()) * 100.0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF040608), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Dezena ${String.format("%02d", entry.key)}", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Text("Repetido: ${entry.value}x (${String.format("%.1f%%", frequencyPercentage)})", color = NeonBlueAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    Divider(color = BorderSlate, modifier = Modifier.padding(vertical = 12.dp))

                    // Estatísticas de dispersão
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Nºs Únicos Diferentes", color = SoftGrayText, fontSize = 10.sp)
                            Text("$unionNumbers / 100", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Dispersão Espacial", color = SoftGrayText, fontSize = 10.sp)
                            val densityCoefficient = (unionNumbers.toDouble() / (comparisonRange * 20.0)) * 100.0
                            Text(String.format("%.1f%%", densityCoefficient), color = NeonPurpleAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // 3. Informações didáticas de suporte
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1117)),
                border = BorderStroke(0.5.dp, BorderSlate),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = NeonBlueAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Análise de Multiciclo: Observar a taxa de dispersão ajuda a compreender a velocidade de esgotamento e desvio padrão de todas as 100 dezenas da mesa.",
                        color = SoftGrayText,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
