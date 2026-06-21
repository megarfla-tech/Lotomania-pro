package com.example.stats

import com.example.db.LotomaniaDraw
import java.util.Random

enum class GeneratorMode(val label: String, val desc: String) {
    RANDOM("Modo Aleatório", "Gera dezenas totalmente aleatórias de forma clássica."),
    STATISTICAL("Modo Estatístico", "Focado das dezenas com melhor desempenho histórico geral."),
    IA_SMART("Modo IA Preditiva", "Aplica pesos de Redes Recorrentes e árvores estatísticas."),
    CONSERVATIVE("Modo Conservador", "Foco em números de baixa volatilidade e frequências estáveis."),
    AGGRESSIVE("Modo Agressivo", "Mistura extrema de números super quentes com os maiores atrasos históricos."),
    BALANCED("Modo Equilibrado", "Garante equilíbrio perfeito de paridade (25P/25I) e preenchimento de quadrantes."),
    TREND("Modo Tendência", "Foco exclusivo nas dezenas com maior variação positiva recente."),
    COLD_ONLY("Modo Números Frios", "Focado apenas nas dezenas mais raras ou atrasadas (Frias)."),
    HOT_ONLY("Modo Números Quentes", "Prioriza as dezenas que mais saem frequentemente (Quentes)."),
    MIXED("Modo Misto", "Mescla equilibrada contendo 50% de números quentes e 50% de frios."),
    ANTI_REPETITION("Modo Anti-Repetição", "Exclui totalmente as 20 dezenas do último concurso sorteado."),
    PROBABILITY_10("Probabilidade 10 Concursos", "Sugere dezenas baseadas prioritariamente nas intensidades probabilísticas recentes do Módulo.")
}

object GameGenerator {

    /**
     * Gera 'count' jogos com 50 dezenas cada, seguindo o 'mode' escolhido
     */
    fun generateGames(
        mode: GeneratorMode,
        count: Int,
        stats: List<NumberStats>,
        lastDrawDezenas: List<Int>,
        allDraws: List<LotomaniaDraw> = emptyList()
    ): List<List<Int>> {
        val games = mutableListOf<List<Int>>()
        val random = Random()

        val sortedByFreq = stats.sortedByDescending { it.freqAbsoluta }
        val hotNumbers = sortedByFreq.take(40).map { it.number }
        val coldNumbers = sortedByFreq.takeLast(40).reversed().map { it.number }

        for (g in 0 until count) {
            val gameSet = mutableSetOf<Int>()

            when (mode) {
                GeneratorMode.RANDOM -> {
                    while (gameSet.size < 50) {
                        gameSet.add(random.nextInt(100))
                    }
                }

                GeneratorMode.STATISTICAL -> {
                    // Seleciona as melhores estatísticas de score de forma prioritária com pequena aleatoriedade
                    val pool = stats.sortedByDescending { it.scoreMisto }.map { it.number }
                    // Insere as 35 melhores e o resto completa de forma aleatória para evitar jogos idênticos
                    gameSet.addAll(pool.take(35))
                    while (gameSet.size < 50) {
                        gameSet.add(random.nextInt(100))
                    }
                }

                GeneratorMode.IA_SMART -> {
                    // Utiliza uma junção das sugestões dos modelos de Machine Learning (Random Forest + LSTM)
                    val rfPool = MachineLearningModels.predictRandomForest(stats).take(25)
                    val lstmPool = MachineLearningModels.predictLstmRecurrent(stats).take(25)
                    gameSet.addAll(rfPool)
                    gameSet.addAll(lstmPool)
                    // Completa o que sobrar de forma aleatória proporcional
                    while (gameSet.size < 50) {
                        val candidate = stats.sortedByDescending { it.scoreMisto * random.nextDouble() }.first().number
                        gameSet.add(candidate)
                    }
                }

                GeneratorMode.CONSERVATIVE -> {
                    // Evita números com altíssima ou baixíssima oscilação (frequência mediana saudável)
                    val midPool = stats.sortedBy { Math.abs(it.freqRelativa - 20.0) }.map { it.number }.take(60)
                    while (gameSet.size < 50) {
                        gameSet.add(midPool[random.nextInt(midPool.size)])
                    }
                }

                GeneratorMode.AGGRESSIVE -> {
                    // 25 mais quentes + 25 mais atrasados
                    val hot = stats.sortedByDescending { it.freqAbsoluta }.map { it.number }.take(25)
                    val delayed = stats.sortedByDescending { it.atraso }.map { it.number }.take(25)
                    gameSet.addAll(hot)
                    gameSet.addAll(delayed)
                    while (gameSet.size < 50) {
                        gameSet.add(random.nextInt(100))
                    }
                }

                GeneratorMode.BALANCED -> {
                    // Equilíbrio de Par/Ímpar: 25 pares e 25 ímpares
                    val pares = (0..99).filter { it % 2 == 0 }
                    val impares = (0..99).filter { it % 2 != 0 }
                    
                    val pSet = mutableSetOf<Int>()
                    while (pSet.size < 25) {
                        pSet.add(pares[random.nextInt(pares.size)])
                    }
                    val iSet = mutableSetOf<Int>()
                    while (iSet.size < 25) {
                        iSet.add(impares[random.nextInt(impares.size)])
                    }
                    gameSet.addAll(pSet)
                    gameSet.addAll(iSet)
                }

                GeneratorMode.TREND -> {
                    // Baseado nos números com maior tendência de crescimento
                    val pool = stats.sortedByDescending { it.tendencia }.map { it.number }.take(45)
                    gameSet.addAll(pool)
                    while (gameSet.size < 50) {
                        gameSet.add(random.nextInt(100))
                    }
                }

                GeneratorMode.COLD_ONLY -> {
                    while (gameSet.size < 50) {
                        gameSet.add(coldNumbers[random.nextInt(coldNumbers.size)])
                    }
                }

                GeneratorMode.HOT_ONLY -> {
                    while (gameSet.size < 50) {
                        gameSet.add(hotNumbers[random.nextInt(hotNumbers.size)])
                    }
                }

                GeneratorMode.MIXED -> {
                    // 25 dezenas quentes e 25 frias
                    val selectedHot = mutableSetOf<Int>()
                    while (selectedHot.size < 25) {
                        selectedHot.add(hotNumbers[random.nextInt(hotNumbers.size)])
                    }
                    val selectedCold = mutableSetOf<Int>()
                    while (selectedCold.size < 25) {
                        selectedCold.add(coldNumbers[random.nextInt(coldNumbers.size)])
                    }
                    gameSet.addAll(selectedHot)
                    gameSet.addAll(selectedCold)
                }

                GeneratorMode.ANTI_REPETITION -> {
                    // Exclui totalmente as dezenas do último concurso
                    val banned = lastDrawDezenas.toSet()
                    val parentPool = (0..99).filter { it !in banned }
                    while (gameSet.size < 50) {
                        gameSet.add(parentPool[random.nextInt(parentPool.size)])
                    }
                }

                GeneratorMode.PROBABILITY_10 -> {
                    // Seleciona a pontuação de maior tendência dos últimos 10 concursos
                    val trendPr = ProbabilityEngine.calculateTrendProbability(allDraws)
                    val pool = trendPr.sortedByDescending { it.trendScore }.map { it.number }
                    // Mantém as 38 melhores de forma fixa e as outras 12 são formadas de forma rotativa da piscina restante do top-60
                    val fixedCount = 38
                    gameSet.addAll(pool.take(fixedCount))
                    val candidates = pool.drop(fixedCount).take(22) // pega os próximos que são fortes
                    while (gameSet.size < 50 && candidates.isNotEmpty()) {
                        gameSet.add(candidates[random.nextInt(candidates.size)])
                    }
                    while (gameSet.size < 50) {
                        gameSet.add(random.nextInt(100))
                    }
                }
            }

            games.add(gameSet.sorted())
        }
        return games
    }
}
