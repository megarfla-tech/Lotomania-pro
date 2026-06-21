package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.LotomaniaDraw
import com.example.ui.theme.*
import java.text.DecimalFormat

data class SimulationStats(
    val totalJogosSimulados: Int,
    val custoTotal: Double,
    val retornoTotal: Double,
    val acertoFrequencia: Map<Int, Int>, // Acertos por faixa (20, 19, 18, 17, 16, 15, 0)
    val lucroPrejuizo: Double,
    val melhorResultado: Int
)

@Composable
fun SimulatorTab(
    draws: List<LotomaniaDraw>,
    generatedGames: List<List<Int>>
) {
    val decimalFormat = remember { DecimalFormat("#,##0.00") }
    var userNumbersStr by remember { mutableStateOf("") }
    var selectedSimulationRange by remember { mutableStateOf(50) } // Útimos 50 concursos para backtest
    var simulationResult by remember { mutableStateOf<SimulationStats?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Calcula simulação sobre os jogos carregados
    fun runBacktestAnimation(customList: List<Int>? = null) {
        val gamesToTest = if (customList != null) {
            listOf(customList)
        } else {
            generatedGames
        }

        if (gamesToTest.isEmpty()) {
            errorMessage = "Nenhum jogo disponível. Gere bilhetes no painel Gerador de Jogos ou digite uma combinação manual para simular."
            simulationResult = null
            return
        }

        errorMessage = null
        val testRange = draws.take(selectedSimulationRange)
        
        var totalCost = 0.0
        var totalWins = 0.0
        val frequencyWins = mutableMapOf<Int, Int>().apply {
            put(20, 0); put(19, 0); put(18, 0); put(17, 0); put(16, 0); put(15, 0); put(0, 0)
        }
        var maxMatches = 0

        for (game in gamesToTest) {
            val gameSet = game.toSet()
            for (draw in testRange) {
                // Lotomania custa R$ 3,00 por aposta de 50 dezenas
                totalCost += 3.00

                // Conta quantos números o bilhete de 50 dezenas acertou no sorteio de 20 dezenas
                val matches = draw.dezenas.count { it in gameSet }
                if (matches > maxMatches) {
                    maxMatches = matches
                }

                // Distribui prêmios baseados em regras oficiais estatísticas estimadas
                when (matches) {
                    20 -> {
                        frequencyWins[20] = frequencyWins[20]!! + 1
                        totalWins += 1800000.00 // Prêmio fictício estimado médio
                    }
                    19 -> {
                        frequencyWins[19] = frequencyWins[19]!! + 1
                        totalWins += 22000.00
                    }
                    18 -> {
                        frequencyWins[18] = frequencyWins[18]!! + 1
                        totalWins += 2000.00
                    }
                    17 -> {
                        frequencyWins[17] = frequencyWins[17]!! + 1
                        totalWins += 200.00
                    }
                    16 -> {
                        frequencyWins[16] = frequencyWins[16]!! + 1
                        totalWins += 35.00
                    }
                    15 -> {
                        frequencyWins[15] = frequencyWins[15]!! + 1
                        totalWins += 10.00
                    }
                    0 -> {
                        frequencyWins[0] = frequencyWins[0]!! + 1
                        totalWins += 130000.00 // Prêmio raro zero acertos
                    }
                }
            }
        }

        simulationResult = SimulationStats(
            totalJogosSimulados = gamesToTest.size * testRange.size,
            custoTotal = totalCost,
            retornoTotal = totalWins,
            acertoFrequencia = frequencyWins,
            lucroPrejuizo = totalWins - totalCost,
            melhorResultado = maxMatches
        )
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
                "Simulador de Investimento & Backtest",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Simule apostas retroativamente contra os sorteios oficiais e avalie seu lucro teórico.",
                color = SoftGrayText,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // 1. Configurações do Backtest (Faixa de concursos)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Período de Simulação Histórica",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val ranges = listOf(10, 50, 100, 500)
                        ranges.forEach { r ->
                            val isSelected = selectedSimulationRange == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) NeonBlueAccent.copy(alpha = 0.2f) else Color(0xFF040608),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonBlueAccent else BorderSlate,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedSimulationRange = r }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val limit = if (r > draws.size) draws.size else r
                                Text(
                                    "Últimos $limit",
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

        // 2. Modos de Ação (Testar dezenas manuais vs Testar jogos gerados ativos)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Origem das Dezenas para o Teste de Impacto",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (generatedGames.isNotEmpty()) {
                        Button(
                            onClick = { runBacktestAnimation() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleAccent),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Simular com os ${generatedGames.size} bilhetes gerados ativos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            "Ou digite dezenas separadas por vírgula:",
                            color = SoftGrayText,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = userNumbersStr,
                        onValueChange = { userNumbersStr = it },
                        label = { Text("Ex: 05, 12, 18, 33, 44... (Exatamente 50 dezenas)", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonBlueAccent,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val parsed = userNumbersStr.split(",")
                                .mapNotNull { it.trim().toIntOrNull() }
                                .filter { it in 0..99 }
                                .distinct()
                            
                            if (parsed.size != 50) {
                                errorMessage = "Sua aposta manual contém exatamente ${parsed.size} dezenas únicas válidas. Lotomania exige exatamente 50 números."
                                simulationResult = null
                            } else {
                                runBacktestAnimation(parsed.sorted())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlueAccent, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Iniciar Simulação Manual", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. Exibição de Mensagens de Erro
        if (errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BloombergRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = BloombergRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage!!, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        // 4. Cartão com Resultados Detalhados da Simulação
        simulationResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070B11)),
                    border = BorderStroke(1.dp, NeonBlueAccent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "MÉTRICAS DO BACKTEST COMPILADAS",
                                color = NeonBlueAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            
                            val balanceColor = if (res.lucroPrejuizo >= 0) BloombergGreen else BloombergRed
                            val balanceIndicatorSymbol = if (res.lucroPrejuizo >= 0) "▲" else "▼"
                            Text(
                                "$balanceIndicatorSymbol R$ " + decimalFormat.format(res.lucroPrejuizo),
                                color = balanceColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Divider(color = BorderSlate, modifier = Modifier.padding(vertical = 12.dp))

                        // Tabela de Balanço financeiro
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Apostas Cruzadas", color = SoftGrayText, fontSize = 10.sp)
                                Text("${res.totalJogosSimulados}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column {
                                Text("Total Investido", color = SoftGrayText, fontSize = 10.sp)
                                Text("R$ " + decimalFormat.format(res.custoTotal), color = BloombergRed, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Retorno Obtido", color = SoftGrayText, fontSize = 10.sp)
                                Text("R$ " + decimalFormat.format(res.retornoTotal), color = BloombergGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Divider(color = BorderSlate, modifier = Modifier.padding(vertical = 12.dp))

                        Text("Melhor faixa atingida:", color = SoftGrayText, fontSize = 10.sp)
                        Text("${res.melhorResultado} acertos", color = PremiumGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid de acertos
                        Text("Detalhamento por Faixas de Acertos:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(20, 19, 18, 17, 16, 15, 0).forEach { score ->
                                val scoreCount = res.acertoFrequencia[score] ?: 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0C1017), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("$score Pontos", color = SoftGrayText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Text("${scoreCount}x", color = if (scoreCount > 0) NeonBlueAccent else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
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
                            "Aviso Legal Sobre Simulações",
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
}
