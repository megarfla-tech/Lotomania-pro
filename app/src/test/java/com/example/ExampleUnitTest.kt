package com.example

import com.example.db.LotomaniaDraw
import com.example.stats.ProbabilityEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testProbabilityEngine_calculatesCorrectly() {
    // 1. Cria draws falsos para os últimos 10 concursos
    val mockDraws = (1..10).map { id ->
      LotomaniaDraw(
        concurso = id,
        data = "20/06/2026",
        // O número 7 aparece em todos os jogos (alta frequência recente)
        // O número 13 aparece apenas no jogo mais recente
        // Os demais números variam deterministicamente
        dezenas = listOf(7, 13) + (20..37).toList(),
        acumulou = false,
        arrecadacao = "R$ 1.000.000,00",
        estimativaProximo = "R$ 2.000.000,00",
        valorAcumulado = "0",
        ganhadores20 = 0, rateio20 = "",
        ganhadores19 = 0, rateio19 = "",
        ganhadores18 = 0, rateio18 = "",
        ganhadores17 = 0, rateio17 = "",
        ganhadores16 = 0, rateio16 = "",
        ganhadores15 = 0, rateio15 = "",
        ganhadores0 = 0, rateio0 = ""
      )
    }

    // Calcula dezenas de maior tendência
    val results = ProbabilityEngine.calculateTrendProbability(mockDraws)

    // A. Verifica se temos exatamente as 100 dezenas representadas
    assertEquals(100, results.size)

    // B. Verifica se exatamente 50 dezenas são apontadas/marcadas para sugestão
    val suggestedCount = results.count { it.isSuggested }
    assertEquals(50, suggestedCount)

    // C. Verifica se o número 7 (que apareceu em 10 sorteios recentes) tem frequência máxima de 10
    val dezena7 = results.first { it.number == 7 }
    assertEquals(10, dezena7.frequency)
    assertEquals(1.0, dezena7.rawProbability, 0.001)

    // D. Verifica se o número 7 é sugerido no Top 50
    assertTrue(dezena7.isSuggested)
  }
}
