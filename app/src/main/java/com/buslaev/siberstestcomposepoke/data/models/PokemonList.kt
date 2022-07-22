package com.buslaev.siberstestcomposepoke.data.models

import com.squareup.moshi.Json

data class PokemonList(
    @field:Json(name = "results")
    val list: List<Result>
)
