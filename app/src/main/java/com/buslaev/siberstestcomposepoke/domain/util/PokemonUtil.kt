package com.buslaev.siberstestcomposepoke.domain.util

import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import com.buslaev.siberstestcomposepoke.presentation.app.Stats

class PokemonUtil {

    fun getSortedList(pokemonList: List<Pokemon>, listOfStats: List<Stats>): List<Pokemon> {
        return pokemonList.map { pokemon ->
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