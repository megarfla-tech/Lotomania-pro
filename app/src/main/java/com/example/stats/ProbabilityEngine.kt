package com.example.stats

import com.example.db.LotomaniaDraw

/**
 * Módulo de cálculo de probabilidade e estimativa de tendência estatística para a Lotomania
 * com base nos últimos 10 concursos de curtíssimo prazo.
 */
data class DezenaProbability(
    val number: Int,
    val frequency: Int,            // Frequência de acertos nos últimos 10 jogos (0 a 10)
    val rawProbability: Double,    // Probabilidade nominal simples baseada na frequência (0.0 a 1.0)
    val recencyWeight: Double,     // Peso de recência (pesando jogos mais recentes com maior relevância)
    val trendScore: Double,        // Score composto balanceado (Frequência e Recência)
    val isSuggested: Boolean       // Indica se pertence ao grupo otimizado das 50 dezenas de maior tendência
)

object ProbabilityEngine {

    /**
     * Calcula a probabilidade e o score de tendência estatística para as 100 dezenas (00 a 99)
     * e retorna o vetor completo ordenado ou anotado para o próximo sorteio.
     */
    fun calculateTrendProbability(allDraws: List<LotomaniaDraw>): List<DezenaProbability> {
        // Pega exatamente as 10 últimas edições da Lotomania
        val recent10 = allDraws.sortedByDescending { it.concurso }.take(10)
        
        val frequencies = IntArray(100)
        val recencyWeights = DoubleArray(100)

        // Calcula a frequência e ponderação de recência para cada dezena
        // recent10[0] é o concurso mais atualizado (peso 1.0)
        // recent10[9] é o menos recente do subgrupo (peso 0.1)
        recent10.forEachIndexed { index, draw ->
            val weight = (10.0 - index.toDouble()) / 10.0 // Ponderação decrescente: 1.0, 0.9, 0.8 ... 0.1
            for (num in draw.dezenas) {
                if (num in 0..99) {
                    frequencies[num]++
                    recencyWeights[num] += weight
                }
            }
        }

        // Gera a lista das 100 dezenas mapeadas
        val candidates = (0..99).map { number ->
            val freq = frequencies[number]
            val rawProb = freq.toDouble() / 10.0
            val recency = recencyWeights[number]
            
            // Fórmula do score de tendência estatística (T) de curtíssimo prazo:
            // Combina a frequência bruta (60% de peso) e o momentum de recência (40% de peso)
            // Somado a um micro-fator de desempate estável para evitar flutuações randômicas.
            val trend = (rawProb * 6.0) + (recency * 4.0) + (number / 1000.0)

            DezenaProbability(
                number = number,
                frequency = freq,
                rawProbability = rawProb,
                recencyWeight = recency,
                trendScore = trend,
                isSuggested = false
            )
        }

        // Ordena por tendência estatística decrescente para selecionar os 50 melhores
        val sortedByTrend = candidates.sortedByDescending { it.trendScore }
        
        // As primeiras 50 dezenas com maior pontuação estatística de tendência
        val suggestedNumbersSet = sortedByTrend.take(50).map { it.number }.toSet()

        // Retorna a lista na ordem numérica convencional (0 a 99) anotando as sugestões oficiais
        return candidates.map { dep ->
            dep.copy(isSuggested = dep.number in suggestedNumbersSet)
        }
    }
}
