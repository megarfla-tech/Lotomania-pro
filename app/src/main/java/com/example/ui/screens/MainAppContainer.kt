package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.LotomaniaDraw
import com.example.stats.GeneratorMode
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LotomaniaViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: LotomaniaViewModel) {
    var hasBypassedSplash by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Sincronização e Estados do ViewModel
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val aiResponse by viewModel.aiAnalysis.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val generatedGames by viewModel.generatedGames.collectAsState()
    val lastUsedMode by viewModel.lastUsedMode.collectAsState()

    var currentTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Stats, 2: IA, 3: Ferramentas, 4: Histórico
    var toolSubTab by remember { mutableStateOf(0) } // 0: Gerador, 1: Simulador, 2: Comparador

    // Se o usuário ainda não passou do Splash Screen / Entrada
    if (!hasBypassedSplash) {
        SplashScreenView(
            onEnterApp = { hasBypassedSplash = true }
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = NeonBlueAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "LOTOMANIA AI PRO",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF030508)
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.syncLatest() },
                        modifier = Modifier.testTag("submit_button")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Sincronizar",
                            tint = NeonBlueAccent
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF030508),
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val items = listOf(
                    Triple("Geral", Icons.Default.Dashboard, 0),
                    Triple("Stats", Icons.Default.Timeline, 1),
                    Triple("IA PRO", Icons.Default.AutoAwesome, 2),
                    Triple("Sistemas", Icons.Default.Casino, 3),
                    Triple("Histórico", Icons.Default.History, 4)
                )

                items.forEach { (label, icon, index) ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = index },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSelected) NeonBlueAccent else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = NeonPurpleAccent.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground)
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NeonBlueAccent)
                            Spacer(Modifier.height(14.dp))
                            Text("Analisando banco histórico estatístico...", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Erro ao inicializar app: ${state.message}", color = BloombergRed, fontSize = 12.sp)
                    }
                }

                is UiState.Success -> {
                    when (currentTab) {
                        0 -> DashboardTab(
                            draws = state.draws,
                            stats = state.stats,
                            isSyncing = isSyncing,
                            syncMessage = syncMessage,
                            onSyncTriggered = { viewModel.syncLatest() },
                            onDismissSyncMessage = { viewModel.clearSyncMessage() },
                            onNavigateToAi = { currentTab = 2 } // Navega para IA
                        )

                        1 -> StatsTab(
                            stats = state.stats,
                            lineStats = state.lineStats,
                            columnStats = state.columnStats,
                            decadeCounts = state.decadeCounts,
                            quadrantStats = state.quadrantStats
                        )

                        2 -> AiTab(
                            stats = state.stats,
                            lastDraw = state.draws.firstOrNull(),
                            aiResponse = aiResponse,
                            isLoading = isAiLoading,
                            onGenerateAiAnalysis = { actStats, actDraw ->
                                viewModel.requestAiAnalysis(actStats, actDraw)
                            }
                        )

                        3 -> {
                            // Sub-abas de ferramentas (Gerador, Simulador, Comparador)
                            Column(modifier = Modifier.fillMaxSize()) {
                                TabRow(
                                    selectedTabIndex = toolSubTab,
                                    containerColor = Color(0xFF030508),
                                    contentColor = Color.White,
                                    indicator = { tabPositions ->
                                        TabRowDefaults.Indicator(
                                            modifier = Modifier.tabIndicatorOffset(tabPositions[toolSubTab]),
                                            color = NeonPurpleAccent
                                        )
                                    }
                                ) {
                                    Tab(
                                        selected = toolSubTab == 0,
                                        onClick = { toolSubTab = 0 },
                                        text = { Text("Gerador", fontSize = 12.sp) }
                                    )
                                    Tab(
                                        selected = toolSubTab == 1,
                                        onClick = { toolSubTab = 1 },
                                        text = { Text("Simulador", fontSize = 12.sp) }
                                    )
                                    Tab(
                                        selected = toolSubTab == 2,
                                        onClick = { toolSubTab = 2 },
                                        text = { Text("Comparador", fontSize = 12.sp) }
                                    )
                                }

                                when (toolSubTab) {
                                    0 -> GeneratorTab(
                                        stats = state.stats,
                                        lastDraw = state.draws.firstOrNull(),
                                        generatedGames = generatedGames,
                                        lastUsedMode = lastUsedMode,
                                        onGenerateGames = { mode, q, st, lD ->
                                            viewModel.generateSelectedGames(mode, q, st, lD)
                                        },
                                        onClearGames = { viewModel.clearGeneratedGames() }
                                    )
                                    1 -> SimulatorTab(
                                        draws = state.draws,
                                        generatedGames = generatedGames
                                    )
                                    2 -> ComparatorTab(
                                        draws = state.draws
                                    )
                                }
                            }
                        }

                        4 -> HistoryTab(
                            draws = state.draws
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreenView(onEnterApp: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Logo animada luxo
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.radialGradient(listOf(NeonPurpleAccent.copy(alpha = 0.3f), Color.Transparent)),
                        CircleShape
                    )
                    .border(2.dp, NeonBlueAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.QueryStats,
                    contentDescription = null,
                    tint = NeonBlueAccent,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "LOTOMANIA AI PRO",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )

            Text(
                "Decodificador Analítico de Redes Neurais",
                color = NeonPurpleAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                "Plataforma avançada de análise preditiva, dispersão espacial por quadrantes, " +
                        "backtests retroativos e inteligência baseada em árvores ensemble.",
                color = SoftGrayText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 40.dp)
            )

            // Botão de bypass imediato
            Button(
                onClick = onEnterApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonBlueAccent,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DoubleArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "ACESSAR SISTEMA DE TELEMETRIA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "Versão Comercial Estável 3.6.1 • Local Offline By-Pass Configurada",
                color = Color.DarkGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
