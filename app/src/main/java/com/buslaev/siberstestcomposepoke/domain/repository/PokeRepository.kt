package com.buslaev.siberstestcomposepoke.domain.repository

import com.buslaev.siberstestcomposepoke.data.locale.dao.AppDao
import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import com.buslaev.siberstestcomposepoke.data.remote.PokeApi
import com.buslaev.siberstestcomposepoke.domain.util.Resource
import kotlinx.coroutines.*
import javax.inject.Inject

interface PokeRepository {

    suspend fun getPokemons(offset: Int, limit: Int): Resource<List<Pokemon>>
    suspend fun getPokemonsFromDao(): List<Pokemon>
    suspend fun insertAllPokemons(list: List<Pokemon>)
    suspend fun deleteAllPokemons()
}

class PokeRepositoryImpl @Inject constructor(
    private val api: PokeApi,
    private val dao: AppDao
) : PokeRepository {
    override suspend fun getPokemons(offset: Int, limit: Int): Resource<List<Pokemon>> {
        return try {
            val data = api.getPokemons(offset = offset, limit = limit)
            val newList = mutableListOf<Pokemon>()
            data.list.forEach {
                println("!!! CALL !!!")
                coroutineScope {
                    val pokemon = api.getPokemon(it.name)
                    newList.add(pokemon)
                }
            }
            Resource.Success(data = newList)
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "An unknown error occurred.")
        }
    }

    override suspend fun getPokemonsFromDao(): List<Pokemon> = dao.getAll()

    override suspend fun insertAllPokemons(list: List<Pokemon>) {
        dao.insertAll(list)
    }

    override suspend fun deleteAllPokemons() {
        dao.deleteAll()
    }
}