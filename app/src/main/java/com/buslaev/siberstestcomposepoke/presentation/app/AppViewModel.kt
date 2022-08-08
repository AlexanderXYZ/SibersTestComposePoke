package com.buslaev.siberstestcomposepoke.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buslaev.siberstestcomposepoke.common.EventHandler
import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import com.buslaev.siberstestcomposepoke.data.remote.PokeApi.Companion.LIMIT
import com.buslaev.siberstestcomposepoke.data.remote.PokeApi.Companion.POKEMON_OFFSET
import com.buslaev.siberstestcomposepoke.domain.repository.PokeRepository
import com.buslaev.siberstestcomposepoke.domain.util.PokemonUtil
import com.buslaev.siberstestcomposepoke.domain.util.Resource
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

data class PokemonStatUi(
    val title: String,
    val value: Int,
    val fontSize: Int
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: PokeRepository,
    private val pokemonUtil: PokemonUtil
) : ViewModel(), EventHandler<MainEvent> {

    var uiState by mutableStateOf(MainState())
    private var currentPage = 0
    private var oldList: List<Pokemon> = emptyList()
    private var onlineFirstLaunch: Boolean = true

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
            loadPokemonListFromCache()
        }
    }

    private fun refreshPokemons(isOnline: Boolean) {
        if (!isOnline) {
            loadPokemonListFromCache()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getPokemons(offset = 0, limit = POKEMON_OFFSET * currentPage)
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
        val data = repository.getPokemons(POKEMON_OFFSET * currentPage, LIMIT)
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

    private fun loadPokemonListFromCache() = viewModelScope.launch(Dispatchers.IO) {
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

        val newPokemonList = if (listOfStats.isEmpty()) {
            oldList
        } else {
            pokemonUtil.getSortedList(
                pokemonList = uiState.pokemonList,
                listOfStats = listOfStats
            )
        }
        uiState = uiState.copy(
            pokemonList = newPokemonList,
            listOfSelectedStats = listOfStats
        )
    }
}