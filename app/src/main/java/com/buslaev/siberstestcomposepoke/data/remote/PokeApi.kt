package com.buslaev.siberstestcomposepoke.data.remote

import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import com.buslaev.siberstestcomposepoke.data.models.PokemonList
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApi {

    companion object {
        const val POKEMON_OFFSET = 30
        const val LIMIT = 30
    }

    @GET("pokemon")
    suspend fun getPokemons(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 30
    ): PokemonList

    @GET("pokemon/{name}")
    suspend fun getPokemon(
        @Path("name") name: String
    ): Pokemon
}