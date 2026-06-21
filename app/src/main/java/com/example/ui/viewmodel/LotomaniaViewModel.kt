package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.db.LotomaniaDatabase
import com.example.db.LotomaniaDraw
import com.example.db.LotomaniaRepository
import com.example.stats.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState {
    object Loading : UiState
    data class Success(
        val draws: List<LotomaniaDraw>,
        val stats: List<NumberStats>,
        val lineStats: Map<Int, Int>,
        val columnStats: Map<Int, Int>,
        val decadeCounts: Map<String, Int>,
        val quadrantStats: QuadrantStats
    ) : UiState
    data class Error(val message: String) : UiState
}

class LotomaniaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = LotomaniaDatabase.getDatabase(application)
    private val repository = LotomaniaRepository(application, database.dao())

    // Estado principal do app em fluxo reativo
    val uiState: StateFlow<UiState> = repository.allDrawsFlow
        .map { draws ->
            if (draws.isEmpty()) {
                // Força o carregamento de sementes se estiver vazio
                repository.getDraws()
                UiState.Loading
            } else {
                val stats = StatsEngine.calculateStats(draws)
                val lineStats = StatsEngine.calculateLineStats(draws)
                val columnStats = StatsEngine.calculateColumnStats(draws)
                val decadeCounts = StatsEngine.calculateDecadeCounts(draws)
                val quadrantStats = StatsEngine.calculateQuadrants(draws)
                
                UiState.Success(
                    draws = draws,
                    stats = stats,
                    lineStats = lineStats,
                    columnStats = columnStats,
                    decadeCounts = decadeCounts,
                    quadrantStats = quadrantStats
                )
            }
        }
        .catch { e -> emit(UiState.Error(e.localizedMessage ?: "Erro desconhecido")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    // Estados secundários e interativos gerenciados na tela
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _aiAnalysis = MutableStateFlow<String?>(null)
    val aiAnalysis: StateFlow<String?> = _aiAnalysis

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    private val _generatedGames = MutableStateFlow<List<List<Int>>>(emptyList())
    val generatedGames: StateFlow<List<List<Int>>> = _generatedGames

    private val _lastUsedMode = MutableStateFlow(GeneratorMode.RANDOM)
    val lastUsedMode: StateFlow<GeneratorMode> = _lastUsedMode

    init {
        // Inicializa o banco de dados carregando as sementes locais se necessário
        viewModelScope.launch {
            repository.getDraws()
        }
    }

    fun syncLatest() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Conectando-se ao servidor oficial da CEF..."
            try {
                val result = repository.syncLatestDraws()
                _syncMessage.value = result
            } catch (e: Exception) {
                _syncMessage.value = "Erro de conexão: Sincronização em segundo plano indisponível."
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    // Chama a API Gemini com inteligência contextual estatística atualizada
    fun requestAiAnalysis(stats: List<NumberStats>, lastDraw: LotomaniaDraw?) {
        if (_isAiLoading.value) return
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiAnalysis.value = "O Lotomania AI PRO está analisando o histórico computacionalmente..."
            
            val hot = stats.sortedByDescending { it.freqAbsoluta }.take(4).map { it.number }
            val cold = stats.sortedBy { it.freqAbsoluta }.take(4).map { it.number }
            val overdue = stats.sortedByDescending { it.atraso }.take(3)
            
            val prompt = """
                Último Concurso Analisado: ${lastDraw?.concurso ?: "Nenhum"} (Data: ${lastDraw?.data ?: "Nenhum"}).
                Números mais frequentes gerais (Quentes): ${hot.joinToString(", ")}.
                Números menos frequentes históricos (Frios): ${cold.joinToString(", ")}.
                Maior Atraso registrado no momento: 
                - Número ${overdue.getOrNull(0)?.number} (Atrasado há ${overdue.getOrNull(0)?.atraso} rodadas)
                - Número ${overdue.getOrNull(1)?.number} (Atrasado há ${overdue.getOrNull(1)?.atraso} rodadas).
                
                Gere uma análise explicada contendo:
                1. Mudanças de comportamento ou tendências recentes.
                2. Um alerta de anomalia estatística (ex: acúmulos por quadrantes ou desvio de paridade).
                3. Sugestão estratégica inteligente explicada para o próximo sorteio.
                Mantenha linguagem simples, polida, profissional e direta ao ponto.
            """.trimIndent()

            val response = GeminiClient.generateAnalysis(prompt)
            _aiAnalysis.value = response
            _isAiLoading.value = false
        }
    }

    // Gera novos jogos matemáticos
    fun generateSelectedGames(mode: GeneratorMode, count: Int, stats: List<NumberStats>, lastDraw: LotomaniaDraw?) {
        _lastUsedMode.value = mode
        val lastDezenas = lastDraw?.dezenas ?: emptyList()
        val results = GameGenerator.generateGames(mode, count, stats, lastDezenas)
        _generatedGames.value = results
    }

    fun clearGeneratedGames() {
        _generatedGames.value = emptyList()
    }
}
