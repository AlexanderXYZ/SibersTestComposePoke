package com.buslaev.siberstestcomposepoke.data.locale.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.buslaev.siberstestcomposepoke.data.models.Pokemon

@Dao
interface AppDao {

    @Query("SELECT * FROM pokemon_table")
    suspend fun getAll(): List<Pokemon>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Pokemon>)

    @Query("DELETE FROM pokemon_table")
    suspend fun deleteAll()
}