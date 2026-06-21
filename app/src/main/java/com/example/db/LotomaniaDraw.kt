package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lotomania_draws")
data class LotomaniaDraw(
    @PrimaryKey val concurso: Int,
    val data: String,
    val dezenas: List<Int>, // 20 dezenas sorteadas de 00 a 99
    val acumulou: Boolean,
    val arrecadacao: String,
    val estimativaProximo: String,
    val valorAcumulado: String,
    val ganhadores20: Int,
    val rateio20: String,
    val ganhadores19: Int,
    val rateio19: String,
    val ganhadores18: Int,
    val rateio18: String,
    val ganhadores17: Int,
    val rateio17: String,
    val ganhadores16: Int,
    val rateio16: String,
    val ganhadores15: Int,
    val rateio15: String,
    val ganhadores0: Int,
    val rateio0: String
)
