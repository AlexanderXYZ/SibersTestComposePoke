package com.buslaev.siberstestcomposepoke.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "pokemon_table")
data class Pokemon(
    @field:Json(name = "id")
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    @field:Json(name = "name")
    val name: String,

    @field:Json(name = "height")
    val height: Int,

    @field:Json(name = "weight")
    val weight: Int,

    @field:Json(name = "sprites")
    val spirites: Spirites,

    @field:Json(name = "stats")
    val stats: List<Stat>,

    @field:Json(name = "types")
    val types: List<Type>
)
