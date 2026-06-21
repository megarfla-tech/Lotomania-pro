package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LotomaniaDao {
    @Query("SELECT * FROM lotomania_draws ORDER BY concurso DESC")
    fun getAllDrawsFlow(): Flow<List<LotomaniaDraw>>

    @Query("SELECT * FROM lotomania_draws ORDER BY concurso DESC")
    suspend fun getAllDraws(): List<LotomaniaDraw>

    @Query("SELECT * FROM lotomania_draws WHERE concurso = :concurso LIMIT 1")
    suspend fun getDrawById(concurso: Int): LotomaniaDraw?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraw(draw: LotomaniaDraw)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(draws: List<LotomaniaDraw>)

    @Query("DELETE FROM lotomania_draws")
    suspend fun deleteAll()
}
