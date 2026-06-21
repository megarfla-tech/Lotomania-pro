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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.LotomaniaDraw
import com.example.stats.DezenaProbability
import com.example.stats.ProbabilityEngine
import com.example.ui.components.RechartsStyledBarChart
import com.example.ui.components.RechartsStyledHeatmap
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProbabilityTab(
    draws: List<LotomaniaDraw>
) {
    val context = LocalContext.current
    
    // Calcula as frequências e probabilidades baseado exclusivamente nos 10 últimos sorteios
    val probabilityData = remember(draws) {
        ProbabilityEngine.calculateTrendProbability(draws)
    }

    // Filtra as 50 dezenas sugeridas para fácil display
    val suggestedNumbers = remember(probabilityData) {
        probabilityData.filter { it.isSuggested }.map { it.number }.sorted()
    }

    var selectedNumber by remember { mutableStateOf<Int?>(null) }
    
    // Caso especial: Seleciona o primeiro sugerido por padrão
    LaunchedEffect(suggestedNumbers) {
        if (selectedNumber == null && suggestedNumbers.isNotEmpty()) {
            selectedNumber = suggestedNumbers.first()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cabeçalho Informativo
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NeonPurpleAccent.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, NeonPurpleAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Troubleshoot,
                                contentDescription = null,
                                tint = NeonPurpleAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                "Módulo Probabilístico (10 Últimos Sorteios)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Ponderação dinâmica de peso decrescente de recência",
                                color = SoftGrayText,
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Análise microscópica que identifica padrões de fluxo de curtíssimo prazo. Os dezenas do último concurso detêm peso máximo (100%), diminuindo até o 10º jogo anterior (10%). O algoritmo une a frequência absoluta recente e o momentum matemático recente para definir as dezenas de maior probabilidade de retorno.",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }

        // Gráfico de Frequência Recente Recharts-Style
        item {
            RechartsStyledBarChart(probabilityData = probabilityData)
        }

        // Mapa de Calor Recharts-Style
        item {
            RechartsStyledHeatmap(probabilityData = probabilityData)
        }

        // 2. Quadro de Dezenas (Matriz 10x10 Compacta e Elegante)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Tabela de Frequência Recente da Lotomania",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        "Cores vívidas sinalizam as 50 dezenas de maior tendência recomendadas.",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 12.dp)
                    )

                    // Matriz 10x10 de dezenas
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (row in 0 until 10) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 0 until 10) {
                                    val number = row * 10 + col
                                    val data = probabilityData.firstOrNull { it.number == number }
                                    val isSelected = selectedNumber == number
                                    val isSuggested = data?.isSuggested ?: false
                                    
                                    // Cores dinâmicas baseadas na pontuação e foco de tendência
                                    val cellBgColor = when {
                                        isSelected -> NeonPurpleAccent
                                        isSuggested -> NeonBlueAccent.copy(alpha = 0.15f)
                                        else -> Color(0xFF0C0E14)
                                    }

                                    val cellBorderColor = when {
                                        isSelected -> NeonPurpleAccent
                                        isSuggested -> NeonBlueAccent.copy(alpha = 0.6f)
                                        else -> BorderSlate
                                    }

                                    val cellTextColor = when {
                                        isSelected -> Color.Black
                                        isSuggested -> NeonBlueAccent
                                        else -> Color.Gray
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(cellBgColor, RoundedCornerShape(6.dp))
                                            .border(1.dp, cellBorderColor, RoundedCornerShape(6.dp))
                                            .clickable { selectedNumber = number }
                                            .testTag("submit_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format("%02d", number),
                                            color = cellTextColor,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSuggested || isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legenda Visual
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(10.dp).background(NeonBlueAccent.copy(alpha = 0.25f), RoundedCornerShape(2.dp)).border(0.5.dp, NeonBlueAccent, RoundedCornerShape(2.dp)))
                            Text("Sugerida (Top 50)", color = SoftGrayText, fontSize = 10.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF0C0E14), RoundedCornerShape(2.dp)).border(0.5.dp, BorderSlate, RoundedCornerShape(2.dp)))
                            Text("Frequência Menor", color = SoftGrayText, fontSize = 10.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(10.dp).background(NeonPurpleAccent, RoundedCornerShape(2.dp)))
                            Text("Selecionada", color = SoftGrayText, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 3. Ficha Estatística Detalhada da Dezena Selecionada
        selectedNumber?.let { number ->
            val data = probabilityData.firstOrNull { it.number == number }
            if (data != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                        border = BorderStroke(1.dp, if (data.isSuggested) NeonBlueAccent.copy(alpha = 0.5f) else BorderSlate),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (data.isSuggested) NeonBlueAccent.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.3f),
                                                CircleShape
                                            )
                                            .border(
                                                1.dp,
                                                if (data.isSuggested) NeonBlueAccent else Color.Gray,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            String.format("%02d", data.number),
                                            color = if (data.isSuggested) NeonBlueAccent else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Column {
                                        Text(
                                            "Análise do Número ${String.format("%02d", data.number)}",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            if (data.isSuggested) "Dezena Qualificada para Próxima Rodada" else "Abaixo do Limiar do Top 50",
                                            color = if (data.isSuggested) BloombergGreen else SoftGrayText,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                
                                val trendPercentage = (data.trendScore / 10.0) * 100.0
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Score de Tendência",
                                        color = SoftGrayText,
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        String.format("%.1f", data.trendScore),
                                        color = if (data.isSuggested) NeonBlueAccent else Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = BorderSlate, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Grid de Métricas específicas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Frequência Recente", color = SoftGrayText, fontSize = 9.sp)
                                    Text("${data.frequency} / 10 Concursos", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Probabilidade Nominal", color = SoftGrayText, fontSize = 9.sp)
                                    Text(String.format("%.0f%%", data.rawProbability * 100.0), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Força de Recência", color = SoftGrayText, fontSize = 9.sp)
                                    Text(String.format("%.2f", data.recencyWeight), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Copiar Dezenas e Dashboard das Sugeridas
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF06090D)),
                border = BorderStroke(1.dp, NeonBlueAccent.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "50 Dezenas Selecionadas de Alta Tendência",
                                color = PremiumGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Completa os 50 números necessários para um volante da Lotomania.",
                                color = SoftGrayText,
                                fontSize = 10.sp
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val dezenasStr = suggestedNumbers.joinToString(", ") { String.format("%02d", it) }
                                val clip = ClipData.newPlainText("Dezenas Recomendadas Lotomania AI", dezenasStr)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "As 50 dezenas recomendadas foram copiadas!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar dezenas", tint = NeonBlueAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Caixa Compacta de Texto com as 50 dezenas separadas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                            .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = suggestedNumbers.joinToString(" • ") { String.format("%02d", it) },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val formatted = suggestedNumbers.joinToString(",")
                            val clip = ClipData.newPlainText("Bilhete Exportado", formatted)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Bilhete com 50 dezenas copiado para Importação!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleAccent, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Copiar em Formato de Jogo (CSV)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
