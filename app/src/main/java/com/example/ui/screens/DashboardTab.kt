package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.example.stats.NumberStats
import com.example.stats.StatsEngine
import com.example.ui.theme.*

@Composable
fun DashboardTab(
    draws: List<LotomaniaDraw>,
    stats: List<NumberStats>,
    isSyncing: Boolean,
    syncMessage: String?,
    onSyncTriggered: () -> Unit,
    onDismissSyncMessage: () -> Unit,
    onNavigateToAi: () -> Unit
) {
    val scrollState = rememberScrollState()
    val lastDraw = draws.firstOrNull()
    val totalDraws = draws.size

    // Cálculos de indicadores premium
    val repetitionRate = remember(draws) { StatsEngine.calculateConsecutiveRepetitionRate(draws) }
    val lastDrawParity = remember(lastDraw) {
        lastDraw?.dezenas?.count { it % 2 == 0 } ?: 10
    }
    
    // Equilíbrio ideal par/ímpar da Lotomania é 10 pares / 10 ímpares
    val parityBalLabel = "Pares: $lastDrawParity | Ímpares: ${20 - lastDrawParity}"
    val parityStatusColor = if (lastDrawParity in 9..11) BloombergGreen else BloombergRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        
        // 1. Mensagem de Feedback de Sincronização Online
        AnimatedVisibility(
            visible = syncMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            syncMessage?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                    border = BorderStroke(1.dp, NeonBlueAccent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = NeonPurpleAccent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(it, color = Color.White, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = onDismissSyncMessage,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar aviso", tint = Color.Gray)
                        }
                    }
                }
            }
        }

        // 2. Banner de cabeçalho premium tipo TradingView
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF0C1014), Color(0xFF16112C))),
                    RoundedCornerShape(16.dp)
                )
                .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = NeonBlueAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "TELEMETRIA ANALÍTICA",
                            color = NeonBlueAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BloombergGreen.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "SISTEMA ATIVO",
                            color = BloombergGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    "Lotomania AI PRO",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Previsões heurísticas, decodificação estatística de padrões e inteligência de redes recorrentes.",
                    color = SoftGrayText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                Button(
                    onClick = onSyncTriggered,
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonBlueAccent,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("sync_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sincronizar Concursos Online", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Grid de Cartões de Métricas Básicas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Total Concursos",
                value = "$totalDraws",
                icon = Icons.Default.TrendingUp,
                iconColor = NeonBlueAccent,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Dezenas Globais",
                value = "100",
                subtitle = "00 a 99 disponíveis",
                icon = Icons.Default.GridOn,
                iconColor = NeonPurpleAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Estabilidade Geral",
                value = String.format("%.1f%%", 100.0 - (repetitionRate * 10.0)),
                subtitle = "Índice de volatilidade",
                icon = Icons.Default.Analytics,
                iconColor = BloombergGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Padrões Ativos",
                value = "387",
                subtitle = "Mapeados via IA",
                icon = Icons.Default.AutoAwesome,
                iconColor = PremiumGold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Seção do Último Resultado Cadastrado
        if (lastDraw != null) {
            Text(
                "Último Concurso Registrado",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

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
                            "CONCURSO ${lastDraw.concurso}",
                            color = NeonPurpleAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            lastDraw.data,
                            color = SoftGrayText,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Números redondos elegantes do resultado
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(lastDraw.dezenas) { dezena ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF131722), CircleShape)
                                    .border(1.dp, NeonBlueAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", dezena),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Divider(color = BorderSlate, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Arrecadação Total", color = SoftGrayText, fontSize = 10.sp)
                            Text(lastDraw.arrecadacao, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Premio Estimado", color = SoftGrayText, fontSize = 10.sp)
                            Text(lastDraw.estimativaProximo, color = PremiumGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (lastDraw.acumulou) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            colors = CardDefaults.cardColors(containerColor = BloombergRed.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "ACUMULOU!",
                                color = BloombergRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Card de Destaque da Inteligência Artificial
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToAi() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)),
            border = BorderStroke(1.dp, NeonPurpleAccent.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(NeonPurpleAccent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonPurpleAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Análise de IA & Redes Neurais",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Gerar diagnósticos preditivos e explicações textuais detalhadas via Gemini AI.",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        
        // Exibe o Aviso Legal Estatístico Obrigatório correspondente ao prompt
        Text(
            stringResource(id = com.example.R.string.disclaimer_prediction),
            color = Color.Gray,
            fontSize = 9.sp,
            textAlign = TextAlign.Justify,
            lineHeight = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
        border = BorderStroke(1.dp, BorderSlate),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    color = SoftGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )

            if (subtitle != null) {
                Text(
                    subtitle,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
