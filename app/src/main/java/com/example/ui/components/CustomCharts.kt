package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stats.NumberStats

// Cores do Tema Financeiro Bloomberg / TradingView
val ColorBackgroundDark = Color(0xFF040608) // Preto profundo
val DarkSlate = Color(0xFF0D0E12)
val NeonBlue = Color(0xFF00E5FF)
val BrightPurple = Color(0xFFB52BFF)
val PositiveGreen = Color(0xFF26E6A4) // Verde Neon
val NegativeRed = Color(0xFFFF4D6A)  // Vermelho Neon
val BorderGray = Color(0xFF1E212A)
val GhostText = Color(0xFF323C50)

@Composable
fun InteractiveHeatmap(
    stats: List<NumberStats>,
    onNumberSelected: (NumberStats) -> Unit
) {
    var selectedNum by remember { mutableStateOf<Int?>(null) }
    
    // Organiza as estatísticas em ordem numérica
    val orderedStats = remember(stats) { stats.sortedBy { it.number } }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSlate, RoundedCornerShape(12.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Mapa de Calor Estatístico (00-99)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Legenda
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(Color(0xFF071226)).border(0.5.dp, BorderGray))
                Spacer(Modifier.width(4.dp))
                Text("Frio", color = Color.Gray, fontSize = 10.sp)
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(10.dp).background(BrightPurple))
                Spacer(Modifier.width(4.dp))
                Text("Medio", color = Color.Gray, fontSize = 10.sp)
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(10.dp).background(NeonBlue))
                Spacer(Modifier.width(4.dp))
                Text("Quente", color = Color.Gray, fontSize = 10.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Grid de 10 linhas por 10 colunas
        Column(modifier = Modifier.fillMaxWidth()) {
            for (row in 0..9) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0..9) {
                        val num = row * 10 + col
                        val stat = orderedStats.getOrNull(num) ?: continue
                        
                        // Define qual é o gradiente de cor baseado na Frequência Relativa
                        val baseColor = when {
                            stat.freqRelativa > 24.0 -> NeonBlue
                            stat.freqRelativa > 20.0 -> BrightPurple
                            stat.freqRelativa > 15.0 -> Color(0xFF6B11B2)
                            else -> Color(0xFF0F1B35)
                        }
                        
                        val isSelected = selectedNum == num
                        val borderCol = if (isSelected) Color.White else BorderGray
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(baseColor, RoundedCornerShape(4.dp))
                                .border(if (isSelected) 1.5.dp else 0.5.dp, borderCol, RoundedCornerShape(4.dp))
                                .clickable {
                                    selectedNum = num
                                    onNumberSelected(stat)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", num),
                                color = if (stat.freqRelativa > 15.0 || isSelected) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        
        selectedNum?.let { num ->
            val stat = orderedStats.getOrNull(num) ?: return@let
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Dezena ${String.format("%02d", num)}",
                    color = NeonBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Sorteado: ${stat.freqAbsoluta}x (${String.format("%.1f", stat.freqRelativa)}%)",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    "Atraso: ${stat.atraso} concursos",
                    color = if (stat.atraso > 15) NegativeRed else PositiveGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DistributionNormalChart(
    title: String,
    data: Map<Int, Int>, // Chave: Linha/Coluna/Década, Valor: Contagem
    modifier: Modifier = Modifier
) {
    val items = data.entries.sortedBy { it.key }
    if (items.isEmpty()) return

    val maxVal = items.maxOf { it.value }.toFloat().coerceAtLeast(1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSlate, RoundedCornerShape(12.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(14.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val width = size.width
            val height = size.height
            val spacing = 20.dp.toPx()
            
            val chartWidth = width - spacing * 2
            val chartHeight = height - spacing * 2
            
            val stepX = chartWidth / (items.size - 1).coerceAtLeast(1)
            
            // Desenha linhas de fundo grade
            val gridLines = 4
            for (g in 0..gridLines) {
                val y = spacing + chartHeight * g / gridLines
                drawLine(
                    color = Color(30, 35, 45, 100),
                    start = Offset(spacing, y),
                    end = Offset(width - spacing, y),
                    strokeWidth = 1f
                )
            }

            // Path da Curva Curvada de Normalização
            val curvePath = Path()
            val pointList = mutableListOf<Offset>()

            for (idx in items.indices) {
                val item = items[idx]
                val x = spacing + idx * stepX
                val proportion = item.value.toFloat() / maxVal
                val y = spacing + chartHeight - (proportion * chartHeight)
                pointList.add(Offset(x, y))

                if (idx == 0) {
                    curvePath.moveTo(x, y)
                } else {
                    val prevPoint = pointList[idx - 1]
                    val controlX = (prevPoint.x + x) / 2
                    curvePath.cubicTo(controlX, prevPoint.y, controlX, y, x, y)
                }
            }

            // Plota o gradiente de preenchimento embaixo da curva
            val fillPath = Path().apply {
                addPath(curvePath)
                lineTo(pointList.last().x, spacing + chartHeight)
                lineTo(pointList.first().x, spacing + chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(BrightPurple.copy(alpha = 0.4f), Color.Transparent),
                    startY = spacing,
                    endY = spacing + chartHeight
                )
            )

            // Linha principal da curva
            drawPath(
                path = curvePath,
                color = NeonBlue,
                style = Stroke(width = 3.dp.toPx())
            )

            // Pontos com círculos neons e textos do cabeçalho
            for (idx in items.indices) {
                val p = pointList[idx]
                val label = items[idx].key.toString()
                
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = p
                )
                
                drawCircle(
                    color = NeonBlue,
                    radius = 8.dp.toPx(),
                    center = p,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Desenha textos dos eixos x no canvas usando o nativeCanvas
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    p.x,
                    height,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 10.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                )

                // Texto do valor de ocorrência
                drawContext.canvas.nativeCanvas.drawText(
                    items[idx].value.toString(),
                    p.x,
                    p.y - 10.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 10.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                )
            }
        }
    }
}

@Composable
fun DecadeDistributionChart(
    decadeCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val items = decadeCounts.entries.toList()
    if (items.isEmpty()) return
    
    val maxVal = items.maxOf { it.value }.toFloat().coerceAtLeast(1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSlate, RoundedCornerShape(12.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            "Distribuição por Décadas",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(14.dp))

        items.forEach { (decade, count) ->
            val ratio = count.toFloat() / maxVal
            val animatedWidth by animateFloatAsState(
                targetValue = ratio,
                animationSpec = tween(durationMillis = 800),
                label = "barWidth"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = decade,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.width(50.dp),
                    fontFamily = FontFamily.Monospace
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(Color(0xFF10141D), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedWidth)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(BrightPurple, NeonBlue)
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(30.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
