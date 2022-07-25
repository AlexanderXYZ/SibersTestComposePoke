package com.buslaev.siberstestcomposepoke.presentation.main

import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import com.buslaev.siberstestcomposepoke.presentation.app.Stats

data class MainState(
    var isFirstLaunch: Boolean = true,
    var isLoading: Boolean = true,
    var error: String = "",
    val pokemonList: List<Pokemon> = emptyList(),
    val selectedPokemon: Pokemon? = null,
    var attackChecked: Boolean = false,
    var defenseChecked: Boolean = false,
    var hpChecked: Boolean = false,
    var listOfSelectedStats: List<Stats> = emptyList()
)


