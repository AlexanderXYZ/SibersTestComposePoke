package com.buslaev.siberstestcomposepoke.data.models

import com.squareup.moshi.Json

data class Spirites(
    @field:Json(name = "front_default")
    val image: String,
)

