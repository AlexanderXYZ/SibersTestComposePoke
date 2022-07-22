package com.buslaev.siberstestcomposepoke.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import com.buslaev.siberstestcomposepoke.domain.repository.PokeRepository
import com.buslaev.siberstestcomposepoke.domain.util.Resource
import com.buslaev.siberstestcomposepoke.presentation.main.MainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: PokeRepository
) : ViewModel() {

    var uiState by mutableStateOf(MainState())
    private var currentPage = 0
    private var oldList: List<Pokemon> = emptyList()
    private var firstLaunch: Boolean = true

    companion object {
        const val POKEMONS_OFFSET = 30
        const val LIMIT = 30
    }

    fun loadPokemons(isOnline: Boolean) {
        if (isOnline) {
            if (firstLaunch) {
                deleteAllDataFromDatabase()
                firstLaunch = false
            }
            loadPokemonsFromServer()
        } else {
            loadPokemonsFromCache()
        }
    }

    fun refreshPokemons() {
        uiState = uiState.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getPokemons(offset = 0, limit = POKEMONS_OFFSET * currentPage)
            when (data) {
                is Resource.Loading -> {
                    uiState = uiState.copy(
                        isLoading = true
                    )
                }
                is Resource.Error -> {

                }
                is Resource.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        attackChecked = false,
                        defenseChecked = false,
                        hpChecked = false,
                        listOfSelectedStats = emptyList(),
                        pokemonList = data.data ?: emptyList()
                    )
                }
            }
        }
    }

    private fun loadPokemonsFromServer() = viewModelScope.launch(Dispatchers.IO) {
        val data = repository.getPokemons(POKEMONS_OFFSET * currentPage, LIMIT)
        when (data) {
            is Resource.Loading -> {
                uiState = uiState.copy(
                    isLoading = true
                )
            }
            is Resource.Error -> {

            }
            is Resource.Success -> {
                currentPage++
                val newList = mutableListOf<Pokemon>()
                newList.addAll(uiState.pokemonList)
                newList.addAll(data.data ?: emptyList())
                oldList = newList
                uiState = uiState.copy(
                    isLoading = false,
                    pokemonList = newList
                )
                insertDataToDatabase(list = newList)
            }
        }
    }

    private fun deleteAllDataFromDatabase() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllPokemons()
    }

    private fun insertDataToDatabase(list: List<Pokemon>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAllPokemons(list)
    }

    private fun loadPokemonsFromCache() = viewModelScope.launch(Dispatchers.IO) {
        val list = repository.getPokemonsFromDao()
        uiState = uiState.copy(
            isLoading = false,
            pokemonList = list
        )
    }

    fun pokemonClick(pokemon: Pokemon) {
        uiState = uiState.copy(
            selectedPokemon = pokemon
        )
    }

    fun checkStats(selectedStat: Stats, isChecked: Boolean) {
        uiState = when (selectedStat) {
            is Stats.Attack -> {
                uiState.copy(
                    attackChecked = isChecked
                )
            }
            is Stats.Defense -> {
                uiState.copy(
                    defenseChecked = isChecked
                )
            }
            is Stats.Hp -> {
                uiState.copy(
                    hpChecked = isChecked
                )
            }
        }

        val listOfStats = mutableListOf<Stats>()
        if (uiState.attackChecked) listOfStats.add(Stats.Attack)
        if (uiState.defenseChecked) listOfStats.add(Stats.Defense)
        if (uiState.hpChecked) listOfStats.add(Stats.Hp)

        uiState = uiState.copy(
            pokemonList = getSortedList(listOfStats),
            listOfSelectedStats = listOfStats
        )
    }

    private fun getSortedList(listOfStats: List<Stats>): List<Pokemon> {
        return uiState.pokemonList.map { pokemon ->
            pokemon.getMaxStatValueByStats(listOfStats)
        }.run {
            this.sortedBy { pair ->
                pair.second
            }.map { sortedPair ->
                sortedPair.getPokemon()
            }.reversed()
        }
    }


    private fun Pokemon.getMaxStatValueByStats(list: List<Stats>): Pair<Pokemon, Int> {
        var maxValue = 0
        list.forEach { stat ->
            val value = this.stats.find { it.statNamed.name == stat.name }?.value
            value?.let {
                if (value > maxValue) maxValue = value
            }
        }
        return Pair(this, maxValue)
    }

    private fun Pair<Pokemon, Int>.getPokemon(): Pokemon = this.first
}

sealed class Stats(val name: String) {
    object Attack : Stats("attack")
    object Defense : Stats("defense")
    object Hp : Stats("hp")
}

