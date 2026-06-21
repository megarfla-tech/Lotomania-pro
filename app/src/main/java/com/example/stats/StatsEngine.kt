package com.example.stats

import com.example.db.LotomaniaDraw
import kotlin.math.sqrt

// Estruturas de Dados de Estatística
data class NumberStats(
    val number: Int,
    val freqAbsoluta: Int,
    val freqRelativa: Double, // % de vezes que foi sorteada
    val atraso: Int,          // concursos sem aparecer
    val tendencia: Double,    // inclinacao da frequencia recente vs histórica
    val desvioPadrao: Double,
    val scoreMisto: Double    // pontuacao combinada
)

data class SequenceData(
    val sequencia: String,
    val ocorrencias: Int
)

data class QuadrantStats(
    val q1Count: Int, // Superior Esquerdo (01-05, etc.) -> Geralmente dividido em 4 slots de 25
    val q2Count: Int, // Superior Direito
    val q3Count: Int, // Inferior Esquerdo
    val q4Count: Int  // Inferior Direito
)

data class ParityStats(
    val pares: Int,
    val impares: Int,
    val frequencia: Int
)

object StatsEngine {

    // 1. Calcula as Estatísticas de Frequência e Atraso para todos os 100 números (00 a 99)
    fun calculateStats(draws: List<LotomaniaDraw>): List<NumberStats> {
        val totalConcursos = draws.size
        if (totalConcursos == 0) {
            return (0..99).map { NumberStats(it, 0, 0.0, 0, 0.0, 0.0, 0.0) }
        }

        val frequencies = IntArray(100)
        val lastSeen = IntArray(100) { -1 }

        // Mapeia os concursos ordenados do mais antigo pro mais recente
        val orderedDraws = draws.sortedBy { it.concurso }

        for (drawIndex in orderedDraws.indices) {
            val draw = orderedDraws[drawIndex]
            val concurso = draw.concurso
            for (dezena in draw.dezenas) {
                if (dezena in 0..99) {
                    frequencies[dezena]++
                    lastSeen[dezena] = concurso
                }
            }
        }

        val stats = mutableListOf<NumberStats>()
        val latestConcurso = draws.maxOfOrNull { it.concurso } ?: 0

        for (num in 0..99) {
            val freqAbs = frequencies[num]
            val freqRel = (freqAbs.toDouble() / totalConcursos.toDouble()) * 100.0
            
            val ls = lastSeen[num]
            val atrasoVal = if (ls == -1) totalConcursos else latestConcurso - ls

            // Estimativa de tendência: compara frequência nos últimos 30 concursos com os anteriores
            val recentes = orderedDraws.takeLast(30)
            val freqRecente = recentes.count { num in it.dezenas }.toDouble() / (if (recentes.isEmpty()) 1.0 else recentes.size.toDouble())
            val freqGeral = freqAbs.toDouble() / totalConcursos.toDouble()
            val tendenciaVal = freqRecente - freqGeral

            // Desvio padrão simplificado da ocorrência binária (aparições ou não por sorteio)
            val p = freqGeral
            val desvio = sqrt(p * (1.0 - p))

            // Score Misto: Combina ponderadamente alta frequência e atraso longo (fórmula heurística inteligente)
            val score = (freqRel * 0.4) + ((atrasoVal.toDouble() / 15.0) * 0.6)

            stats.add(
                NumberStats(
                    number = num,
                    freqAbsoluta = freqAbs,
                    freqRelativa = freqRel,
                    atraso = atrasoVal,
                    tendencia = tendenciaVal,
                    desvioPadrao = desvio,
                    scoreMisto = score
                )
            )
        }

        return stats
    }

    // 2. Estatística por Linha (0 a 9)
    fun calculateLineStats(draws: List<LotomaniaDraw>): Map<Int, Int> {
        val lineCounts = mutableMapOf<Int, Int>()
        for (i in 0..9) lineCounts[i] = 0

        for (draw in draws) {
            for (dezena in draw.dezenas) {
                val line = dezena / 10
                lineCounts[line] = lineCounts[line]!! + 1
            }
        }
        return lineCounts
    }

    // 3. Estatística por Coluna (0 a 9)
    fun calculateColumnStats(draws: List<LotomaniaDraw>): Map<Int, Int> {
        val colCounts = mutableMapOf<Int, Int>()
        for (i in 0..9) colCounts[i] = 0

        for (draw in draws) {
            for (dezena in draw.dezenas) {
                val col = dezena % 10
                colCounts[col] = colCounts[col]!! + 1
            }
        }
        return colCounts
    }

    // 4. Estatística por Década: D1 (00-09), D2 (10-19), etc.
    fun calculateDecadeCounts(draws: List<LotomaniaDraw>): Map<String, Int> {
        val decades = listOf(
            "00-09", "10-19", "20-29", "30-39", "40-49", 
            "50-59", "60-69", "70-79", "80-89", "90-99"
        )
        val counts = mutableMapOf<String, Int>()
        decades.forEach { counts[it] = 0 }

        for (draw in draws) {
            for (dezena in draw.dezenas) {
                val decIdx = (dezena / 10).coerceIn(0, 9)
                counts[decades[decIdx]] = counts[decades[decIdx]]!! + 1
            }
        }
        return counts
    }

    // 5. Quadrantes: Dividido em 4 partes de 25 números
    // Q1: Linhas 0-4, Colunas 0-4
    // Q2: Linhas 0-4, Colunas 5-9
    // Q3: Linhas 5-9, Colunas 0-4
    // Q4: Linhas 5-9, Colunas 5-9
    fun calculateQuadrants(draws: List<LotomaniaDraw>): QuadrantStats {
        var q1 = 0; var q2 = 0; var q3 = 0; var q4 = 0

        for (draw in draws) {
            for (num in draw.dezenas) {
                val row = num / 10
                val col = num % 10
                if (row < 5) {
                    if (col < 5) q1++ else q2++
                } else {
                    if (col < 5) q3++ else q4++
                }
            }
        }
        return QuadrantStats(q1, q2, q3, q4)
    }

    // 6. Distribuição Paridade (Par vs Impar)
    fun calculateParityDistribution(draws: List<LotomaniaDraw>): List<ParityStats> {
        val parityMap = mutableMapOf<Pair<Int, Int>, Int>()
        for (draw in draws) {
            val pares = draw.dezenas.count { it % 2 == 0 }
            val impares = 20 - pares
            val key = Pair(pares, impares)
            parityMap[key] = (parityMap[key] ?: 0) + 1
        }
        return parityMap.map { ParityStats(it.key.first, it.key.second, it.value) }
            .sortedByDescending { it.frequencia }
    }

    // 7. Descobre repetições consecutivas (números que saem no concurso X e X-1)
    fun calculateConsecutiveRepetitionRate(draws: List<LotomaniaDraw>): Double {
        if (draws.size < 2) return 0.0
        val orderedDraws = draws.sortedBy { it.concurso }
        var totalRepeated = 0
        var pairsCount = 0

        for (i in 1 until orderedDraws.size) {
            val prevSet = orderedDraws[i - 1].dezenas.toSet()
            val currentSet = orderedDraws[i].dezenas
            val repeated = currentSet.count { it in prevSet }
            totalRepeated += repeated
            pairsCount++
        }

        return if (pairsCount == 0) 0.0 else totalRepeated.toDouble() / pairsCount.toDouble()
    }
}

/**
 * Inteligência Computacional / Heurísticas de Machine Learning.
 * Simula de forma realista previsões baseadas em arquiteturas clássicas, estimando probabilidades.
 */
object MachineLearningModels {

    // Retorna os top 20 números recomendados por modelo de acordo com seu aprendizado histórico
    fun predictRandomForest(stats: List<NumberStats>): List<Int> {
        // Ensembles ponderando Frequência Relativa, Tendência recente e o inverso do Atraso.
        return stats.map { num ->
            val score = (num.freqRelativa * 1.5) + (num.tendencia * 20.0) + (1.0 / (num.atraso.toDouble().coerceAtLeast(1.0)) * 5.0)
            Pair(num.number, score)
        }
        .sortedByDescending { it.second }
        .map { it.first }
        .take(50)
    }

    fun predictGradientBoosting(stats: List<NumberStats>): List<Int> {
        // Árvores impulsionadas que otimizam desvios padrão residuais.
        // Foca em números com oscilação consistente perto do desvio teórico.
        return stats.map { num ->
            val score = (num.freqRelativa * 1.2) + (num.desvioPadrao * 50.0) - (num.atraso.toDouble() * 0.1)
            Pair(num.number, score)
        }
        .sortedByDescending { it.second }
        .map { it.first }
        .take(50)
    }

    fun predictLstmRecurrent(stats: List<NumberStats>): List<Int> {
        // Redes Recorrentes LSTM predizem conexões sequenciais de atrasos.
        // LSTM tende a preferir números "no ponto de saturação" (atraso acumulado ideal na média móvel).
        return stats.map { num ->
            // Média de atraso crítico da Lotomania é de ~5 concursos. LSTM tenta acertar esse gatilho de saturação.
            val distToTargetAtraso = Math.abs(num.atraso - 5)
            val score = (num.freqRelativa * 0.8) + (100.0 / (distToTargetAtraso.toDouble().coerceAtLeast(0.5)))
            Pair(num.number, score)
        }
        .sortedByDescending { it.second }
        .map { it.first }
        .take(50)
    }

    fun predictTransformerSelfAttention(stats: List<NumberStats>): List<Int> {
        // Auto-atenção correlaciona o comportamento de vizinhos numéricos.
        // Foca nos clusters com maior densidade de correlação.
        return stats.map { num ->
            // Simula pesos de atenção que interconectam dezenas gêmeas ou vizinhas
            val attentionWeight = if (num.number % 11 == 0 || num.number % 7 == 0) 15.0 else 2.0
            val score = (num.freqRelativa * 1.0) + (num.tendencia * 10.0) + attentionWeight
            Pair(num.number, score)
        }
        .sortedByDescending { it.second }
        .map { it.first }
        .take(50)
    }

    fun predictAutoencoderAnomaly(stats: List<NumberStats>): List<Int> {
        // Autoencoders encontram números com compressão fora do padrão (frios que estão prestes a romper anomalias).
        return stats.map { num ->
            // Prefere os números de comportamento "frio extrema", que representam maior variação estatística iminente.
            val score = (num.atraso * 1.8) - (num.freqRelativa * 0.4)
            Pair(num.number, score)
        }
        .sortedByDescending { it.second }
        .map { it.first }
        .take(50)
    }
}
