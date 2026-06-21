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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.stats.DezenaProbability
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape

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

@Composable
fun RechartsStyledBarChart(
    probabilityData: List<DezenaProbability>,
    modifier: Modifier = Modifier
) {
    // We show the top 15 most frequent or strongest trend numbers in the 10 last games
    val sortedItems = remember(probabilityData) {
        probabilityData.sortedByDescending { it.trendScore }.take(15)
    }
    
    var selectedItemIndex by remember { mutableStateOf<Int?>(null) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderGray, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSlate),
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
                        "Gráfico de Frequência Recente",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Distribuição das 15 dezenas com maior pontuação recente",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                
                // Badge de 10 jogos
                Box(
                    modifier = Modifier
                        .background(NeonBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("10 Jogos", color = NeonBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Gráfico de Barras com Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val maxFrequency = 10f // Como analisamos 10 jogos, o máximo teórico é 10
                
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    
                    val paddingLeft = 30.dp.toPx()
                    val paddingRight = 10.dp.toPx()
                    val paddingTop = 10.dp.toPx()
                    val paddingBottom = 25.dp.toPx()
                    
                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom
                    
                    // 1. Linhas de Grade de Y (0, 2, 4, 6, 8, 10 de ocorrência)
                    val yTicks = listOf(0f, 2f, 4f, 6f, 8f, 10f)
                    for (tick in yTicks) {
                        val fraction = tick / maxFrequency
                        val y = paddingTop + chartHeight - (fraction * chartHeight)
                        
                        // Grade
                        drawLine(
                            color = Color(255, 255, 255, 20),
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1f
                        )
                        
                        // Legendas de Y
                        drawContext.canvas.nativeCanvas.drawText(
                            "${tick.toInt()}x",
                            paddingLeft - 8.dp.toPx(),
                            y + 4.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 9.dp.toPx()
                                textAlign = android.graphics.Paint.Align.RIGHT
                                typeface = android.graphics.Typeface.MONOSPACE
                            }
                        )
                    }
                    
                    // 2. Desenha cada barra
                    val barSpacing = 8.dp.toPx()
                    val totalBars = sortedItems.size
                    val totalSpacing = barSpacing * (totalBars - 1)
                    val barWidth = (chartWidth - totalSpacing) / totalBars
                    
                    for (i in 0 until totalBars) {
                        val item = sortedItems[i]
                        val x = paddingLeft + i * (barWidth + barSpacing)
                        
                        // Frequência real na amostra de 10 jogos
                        val freq = item.frequency.toFloat()
                        val rawRatio = freq / maxFrequency
                        
                        val barHeight = chartHeight * rawRatio
                        val barTopY = paddingTop + chartHeight - barHeight
                        
                        val isBarHovered = selectedItemIndex == i
                        
                        val brush = Brush.verticalGradient(
                            colors = if (isBarHovered) {
                                listOf(BrightPurple, NeonBlue)
                            } else {
                                listOf(NeonBlue.copy(alpha = 0.85f), BrightPurple.copy(alpha = 0.5f))
                            }
                        )
                        
                        // RENDER DA BARRA COM CANTO ARREDONDADO SUPERIOR
                        drawRoundRect(
                            brush = brush,
                            topLeft = Offset(x, barTopY),
                            size = Size(barWidth, barHeight.coerceAtLeast(1f)),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        
                        if (isBarHovered) {
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(x - 2f, barTopY - 2f),
                                size = Size(barWidth + 4f, barHeight + 2f),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                        
                        // Legendas do eixo X (Número)
                        val numStr = String.format("%02d", item.number)
                        drawContext.canvas.nativeCanvas.drawText(
                            numStr,
                            x + barWidth / 2f,
                            height - 6.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = if (isBarHovered) android.graphics.Color.WHITE else android.graphics.Color.GRAY
                                textSize = 10.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                typeface = android.graphics.Typeface.MONOSPACE
                                isFakeBoldText = isBarHovered
                            }
                        )
                        
                        // Valor acima da barra
                        if (item.frequency > 0) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "${item.frequency}x",
                                x + barWidth / 2f,
                                barTopY - 4.dp.toPx(),
                                android.graphics.Paint().apply {
                                    color = if (isBarHovered) android.graphics.Color.CYAN else android.graphics.Color.LTGRAY
                                    textSize = 8.dp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                }
                            )
                        }
                    }
                }
                
                // Camada transparente de cliques interativos sobre o Canvas para habilitar o Tooltip
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 30.dp, end = 10.dp, bottom = 25.dp, top = 10.dp)
                ) {
                    sortedItems.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedItemIndex = if (selectedItemIndex == index) null else index }
                        )
                    }
                }
            }
            
            // Tooltip de Detalhes Dinâmicos (Recharts Tooltip)
            Spacer(modifier = Modifier.height(10.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedItemIndex != null) {
                    val activeItem = sortedItems[selectedItemIndex!!]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                            .border(1.dp, NeonBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(NeonBlue.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, NeonBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", activeItem.number),
                                    color = NeonBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column {
                                Text("Amostra Recente de 10 Jogos", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Frequência de aparições: ${activeItem.frequency}", color = Color.Gray, fontSize = 9.sp)
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Probabilidade / Score", color = Color.Gray, fontSize = 9.sp)
                            Text(
                                text = "${String.format("%.0f%%", activeItem.rawProbability * 100.0)} / ${String.format("%.1f", activeItem.trendScore)}",
                                color = BrightPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    Text(
                        "Dica de Toque: Selecione qualquer coluna do gráfico para ver a dica interativa de tendência (estilo Tooltip do Recharts).",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RechartsStyledHeatmap(
    probabilityData: List<DezenaProbability>,
    modifier: Modifier = Modifier
) {
    var selectedNumber by remember { mutableStateOf<Int?>(null) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderGray, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSlate),
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
                        "Mapa de Calor de Tendência (Heatmap)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Distribuição de frequência térmica nos últimos 10 concursos",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(BrightPurple.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("10 Sorteios", color = BrightPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Grid de mapa de calor 10x10 elegante
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (row in 0 until 10) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until 10) {
                            val number = row * 10 + col
                            val data = probabilityData.firstOrNull { it.number == number }
                            val freq = data?.frequency ?: 0
                            val isSelected = selectedNumber == number
                            
                            // Determinando as cores de calor com gradiente/espectro Recharts
                            val cellBgColor = when (freq) {
                                0, 1 -> Color(0xFF0F141C) // Frio (Black Blue)
                                2 -> Color(0xFF1B2E47)    // Muted Cool Blue
                                3 -> Color(0xFF2C4A73)    // Neutro Baixo
                                4 -> Color(0xFF4A4E9E)    // Neutras
                                5 -> Color(0xFF673AB7)    // Médio Alto (Roxo)
                                6 -> Color(0xFF9C27B0)    // Relevante
                                7 -> Color(0xFFE91E63)    // Quente (Rosa Forte)
                                8 -> Color(0xFFFF5722)    // Muito Quente (Laranja Chama)
                                9, 10 -> Color(0xFFFFC107)// Máximo (Ouro Neon)
                                else -> Color(0xFF0F141C)
                            }
                            
                            val cellBorderColor = if (isSelected) {
                                Color.White
                            } else {
                                cellBgColor.copy(alpha = 0.4f)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(cellBgColor, RoundedCornerShape(4.dp))
                                    .border(
                                        if (isSelected) 1.5.dp else 0.5.dp, 
                                        cellBorderColor, 
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { selectedNumber = if (selectedNumber == number) null else number },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", number),
                                    color = if (freq >= 4 || isSelected) Color.White else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Legenda de Espectro Recharts (Gradiente de Frio a Quente)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Frio (0x)", color = Color.Gray, fontSize = 9.sp)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(6.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F141C),
                                    Color(0xFF1B2E47),
                                    Color(0xFF4A4E9E),
                                    Color(0xFF9C27B0),
                                    Color(0xFFFF5722),
                                    Color(0xFFFFC107)
                                )
                            ),
                            RoundedCornerShape(3.dp)
                        )
                )
                
                Text("Quente (10x)", color = Color.Gray, fontSize = 9.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tooltip do elemento ativo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedNumber != null) {
                    val activeNum = selectedNumber!!
                    val activeData = probabilityData.firstOrNull { it.number == activeNum }
                    if (activeData != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFF5722).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFFF5722).copy(alpha = 0.15f), CircleShape)
                                        .border(1.dp, Color(0xFFFF5722), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%02d", activeData.number),
                                        color = Color(0xFFFF5722),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column {
                                    Text("Dezena ${String.format("%02d", activeData.number)} analisada", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Aparições: ${activeData.frequency} sorteios nos últimos 10", color = Color.Gray, fontSize = 9.sp)
                                }
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Score de Tendência", color = Color.Gray, fontSize = 9.sp)
                                Text(
                                    text = String.format("%.2f", activeData.trendScore),
                                    color = Color(0xFFFFC107),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "Dica: Toque em qualquer quadrado do mapa de calor para ver dados precisos de frequência e pontuação de tendência da dezena.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
