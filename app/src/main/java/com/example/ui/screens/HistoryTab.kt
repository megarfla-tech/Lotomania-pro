package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.LotomaniaDraw
import com.example.ui.theme.*

@Composable
fun HistoryTab(draws: List<LotomaniaDraw>) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedConcurso by remember { mutableStateOf<Int?>(null) }
    var favorites by remember { mutableStateOf(setOf<Int>()) }

    val filteredDraws = remember(draws, searchQuery) {
        if (searchQuery.isBlank()) {
            draws
        } else {
            draws.filter {
                it.concurso.toString().contains(searchQuery) || it.data.contains(searchQuery)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        Text(
            "Histórico Geral Lotomania",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Pesquise detalhes de arrecadação, rateio de prêmios e dezenas de concursos passados.",
            color = SoftGrayText,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        // Barra de Busca Inteligente
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Pesquise por concurso ou data (Ex: 2630 ou dd/mm)...", fontSize = 12.sp, color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpar busca", tint = Color.Gray)
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonBlueAccent,
                unfocusedBorderColor = BorderSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredDraws.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum concurso encontrado para esta busca.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            items(filteredDraws) { draw ->
                val isExpanded = expandedConcurso == draw.concurso
                val isFavorite = favorites.contains(draw.concurso)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedConcurso = if (isExpanded) null else draw.concurso
                        }
                        .border(
                            1.dp,
                            if (isExpanded) NeonBlueAccent.copy(alpha = 0.5f) else BorderSlate,
                            RoundedCornerShape(10.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "CONCURSO ${draw.concurso}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    draw.data,
                                    color = SoftGrayText,
                                    fontSize = 11.sp
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        favorites = if (isFavorite) {
                                            favorites - draw.concurso
                                        } else {
                                            favorites + draw.concurso
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favoritar",
                                        tint = if (isFavorite) BloombergRed else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Imprime dezenas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF030508), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = draw.dezenas.joinToString(" • ") { String.format("%02d", it) },
                                color = NeonBlueAccent,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Detalhamento de prêmios expandido (Rateios CEF oficiais)
                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Divider(color = BorderSlate, modifier = Modifier.padding(bottom = 12.dp))
                                
                                Text(
                                    "Tabela de Prêmios & Rateio Oficial:",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val prizes = listOf(
                                    Triple("20 Acertos", draw.ganhadores20, draw.rateio20),
                                    Triple("19 Acertos", draw.ganhadores19, draw.rateio19),
                                    Triple("18 Acertos", draw.ganhadores18, draw.rateio18),
                                    Triple("17 Acertos", draw.ganhadores17, draw.rateio17),
                                    Triple("16 Acertos", draw.ganhadores16, draw.rateio16),
                                    Triple("15 Acertos", draw.ganhadores15, draw.rateio15),
                                    Triple("0 Acertos", draw.ganhadores0, draw.rateio0)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    prizes.forEach { (faixa, winners, payout) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(faixa, color = SoftGrayText, fontSize = 10.sp)
                                            Row {
                                                Text("$winners ganhadores", color = Color.Gray, fontSize = 10.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(payout, color = if (winners > 0) BloombergGreen else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Arrecadação Total", color = SoftGrayText, fontSize = 9.sp)
                                        Text(draw.arrecadacao, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Acumulado p/ Próximo", color = SoftGrayText, fontSize = 9.sp)
                                        Text(draw.valorAcumulado, color = PremiumGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
