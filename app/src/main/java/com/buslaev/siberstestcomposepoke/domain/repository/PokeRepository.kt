package com.buslaev.siberstestcomposepoke.domain.repository

import com.buslaev.siberstestcomposepoke.data.locale.dao.AppDao
import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import com.buslaev.siberstestcomposepoke.data.remote.PokeApi
import com.buslaev.siberstestcomposepoke.domain.util.Resource
import kotlinx.coroutines.*
import javax.inject.Inject

interface PokeRepository {

    /**
     * Get pokemons by paginating from remote server
     * @return list of pokemonds wrapped in class resource
     */
    suspend fun getPokemons(offset: Int, limit: Int): Resource<List<Pokemon>>

    /**
     * Return list of pokemons from cache (Database)
     * @return list of pokemons
     */
    suspend fun getPokemonsFromDatabase(): List<Pokemon>

    /**
     * Insert list of pokemons to cache (Database)
     */
    suspend fun insertAllPokemonsToDatabase(list: List<Pokemon>)

    /**
     * Clear database
     */
    suspend fun deleteAllPokemonsFromDatabase()
}

class PokeRepositoryImpl @Inject constructor(
    private val api: PokeApi,
    private val dao: AppDao
) : PokeRepository {

    override suspend fun getPokemons(offset: Int, limit: Int): Resource<List<Pokemon>> {
        return try {
            val data = api.getPokemons(offset = offset, limit = limit)
            val newList = mutableListOf<Pokemon>()
            coroutineScope {
                data.list.forEach {
                    launch {
                        val pokemon = api.getPokemon(it.name)
                        newList.add(pokemon)
                    }
                }
            }
            Resource.Success(data = newList.sortedBy { it.id })
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getPokemonsFromDatabase(): List<Pokemon> = dao.getAll()

    override suspend fun insertAllPokemonsToDatabase(list: List<Pokemon>) {
        dao.insertAll(list)
    }

    override suspend fun deleteAllPokemonsFromDatabase() {
        dao.deleteAll()
    }
}