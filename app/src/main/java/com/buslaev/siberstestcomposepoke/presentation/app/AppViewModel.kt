package com.buslaev.siberstestcomposepoke.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buslaev.siberstestcomposepoke.common.EventHandler
import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import com.buslaev.siberstestcomposepoke.domain.repository.PokeRepository
import com.buslaev.siberstestcomposepoke.domain.util.Resource
import com.buslaev.siberstestcomposepoke.presentation.app.AppViewModel.Companion.POKEMONS_OFFSET
import com.buslaev.siberstestcomposepoke.presentation.main.MainEvent
import com.buslaev.siberstestcomposepoke.presentation.main.MainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Stats(val name: String) {
    object Attack : Stats("attack")
    object Defense : Stats("defense")
    object Hp : Stats("hp")
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: PokeRepository
) : ViewModel(), EventHandler<MainEvent> {

    var uiState by mutableStateOf(MainState())
    private var currentPage = 0
    private var oldList: List<Pokemon> = emptyList()
    private var onlineFirstLaunch: Boolean = true

    companion object {
        const val POKEMONS_OFFSET = 30
        const val LIMIT = 30
    }

    override fun obtainEvent(event: MainEvent) {
        when (event) {
            is MainEvent.LoadPokemons -> loadPokemons(isOnline = event.isOnline)
            is MainEvent.RefreshPokemonsClicked -> refreshPokemons(event.isOnline)
            is MainEvent.PokemonClicked -> pokemonClick(pokemon = event.pokemon)
            is MainEvent.CheckBoxClicked -> checkStats(
                selectedStat = event.selectedStat,
                isChecked = event.isChecked
            )
        }
    }

    private fun loadPokemons(isOnline: Boolean) {
        uiState = uiState.copy(
            isLoading = true,
            isFirstLaunch = false,
            error = ""
        )

        if (isOnline) {
            if (onlineFirstLaunch) {
                deleteAllDataFromDatabase()
                onlineFirstLaunch = false
            }
            loadPokemonsFromServer()
        } else {
            loadPokemonsFromCache()
        }
    }

    private fun refreshPokemons(isOnline: Boolean) {
        if (!isOnline) {
            loadPokemonsFromCache()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getPokemons(offset = 0, limit = POKEMONS_OFFSET * currentPage)
            when (data) {
                is Resource.Loading -> {
                    uiState = uiState.copy(
                        isLoading = true
                    )
                }
                is Resource.Error -> {
                    uiState = uiState.copy(
                        error = data.message ?: ""
                    )
                }
                is Resource.Success -> {
                    val newList = data.data ?: emptyList()
                    oldList = newList
                    uiState = uiState.copy(
                        isLoading = false,
                        attackChecked = false,
                        defenseChecked = false,
                        hpChecked = false,
                        listOfSelectedStats = emptyList(),
                        pokemonList = newList
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
                uiState = uiState.copy(
                    error = data.message ?: ""
                )
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
        repository.deleteAllPokemonsFromDatabase()
    }

    private fun insertDataToDatabase(list: List<Pokemon>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAllPokemonsToDatabase(list)
    }

    private fun loadPokemonsFromCache() = viewModelScope.launch(Dispatchers.IO) {
        val list = repository.getPokemonsFromDatabase()
        oldList = list
        uiState = uiState.copy(
            isLoading = false,
            attackChecked = false,
            defenseChecked = false,
            hpChecked = false,
            listOfSelectedStats = emptyList(),
            pokemonList = list
        )
    }

    private fun pokemonClick(pokemon: Pokemon) {
        uiState = uiState.copy(
            selectedPokemon = pokemon
        )
    }

    private fun checkStats(selectedStat: Stats, isChecked: Boolean) {
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

        val newPokemonList = if (listOfStats.isEmpty()) oldList else getSortedList(listOfStats)
        uiState = uiState.copy(
            pokemonList = newPokemonList,
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