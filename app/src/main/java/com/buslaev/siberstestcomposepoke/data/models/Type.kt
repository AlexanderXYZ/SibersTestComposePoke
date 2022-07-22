package com.buslaev.siberstestcomposepoke.data.models

import com.squareup.moshi.Json

data class Type(
    @field:Json(name = "type")
    val desctiption: Description
)

data class Description(
    @field:Json(name = "name")
    val name: String
)
