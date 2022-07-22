package com.buslaev.siberstestcomposepoke.data.models

import com.squareup.moshi.Json

data class Stat(
    @field:Json(name = "base_stat")
    val value: Int,

    @field:Json(name = "stat")
    val statNamed: StatNamed
)


data class StatNamed(
    @field:Json(name = "name")
    val name: String
)
