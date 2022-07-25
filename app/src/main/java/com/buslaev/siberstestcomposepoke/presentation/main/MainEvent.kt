package com.buslaev.siberstestcomposepoke.presentation.main

import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import com.buslaev.siberstestcomposepoke.presentation.app.Stats

sealed class MainEvent {
    data class LoadPokemons(val isOnline: Boolean) : MainEvent()
    object RefreshPokemonsClicked : MainEvent()
    data class PokemonClicked(val pokemon: Pokemon) : MainEvent()
    data class CheckBoxClicked(val selectedStat: Stats, val isChecked: Boolean) : MainEvent()
}
