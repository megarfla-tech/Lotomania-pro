package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stats.QuadrantStats
import com.example.stats.NumberStats
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun StatsTab(
    stats: List<NumberStats>,
    lineStats: Map<Int, Int>,
    columnStats: Map<Int, Int>,
    decadeCounts: Map<String, Int>,
    quadrantStats: QuadrantStats
) {
    val scrollState = rememberScrollState()
    var lastSelectedStats by remember { mutableStateOf<NumberStats?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            "Análise Estatística Avançada",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            "Consulte padrões de frequência absoluta, atrasos em tempo real e distribuição espacial de dezenas.",
            color = SoftGrayText,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1. Grid Mapa de Calor Interativo
        InteractiveHeatmap(
            stats = stats,
            onNumberSelected = { selected ->
                lastSelectedStats = selected
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Quadro de Distribuição por Quadrantes
        QuadrantSummaryCard(quadrantStats)

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Distribuição por Décadas
        DecadeDistributionChart(decadeCounts = decadeCounts)

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Curva de Distribuição Normal (Linhas e Colunas)
        DistributionNormalChart(
            title = "Ocorrência por Linhas (0 a 9)",
            data = lineStats
        )

        Spacer(modifier = Modifier.height(20.dp))

        DistributionNormalChart(
            title = "Ocorrência por Colunas (Finais 0 a 9)",
            data = columnStats
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun QuadrantSummaryCard(quadrants: QuadrantStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSlate, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Distribuição por Quadrantes",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Layout 2x2 simulando o volante Lotomania dividido em quadrantes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
            ) {
                // Linha de cima (Q1 e Q2)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.25.dp, BorderSlate)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Q1 (S.E.)", color = SoftGrayText, fontSize = 10.sp)
                            Text("${quadrants.q1Count}x", color = NeonBlueAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.25.dp, BorderSlate)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Q2 (S.D.)", color = SoftGrayText, fontSize = 10.sp)
                            Text("${quadrants.q2Count}x", color = NeonPurpleAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                // Linha de baixo (Q3 e Q4)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.25.dp, BorderSlate)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Q3 (I.E.)", color = SoftGrayText, fontSize = 10.sp)
                            Text("${quadrants.q3Count}x", color = BrightPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.25.dp, BorderSlate)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Q4 (I.D.)", color = SoftGrayText, fontSize = 10.sp)
                            Text("${quadrants.q4Count}x", color = BloombergGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
