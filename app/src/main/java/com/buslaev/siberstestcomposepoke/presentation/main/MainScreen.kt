package com.buslaev.siberstestcomposepoke.presentation.main

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.buslaev.siberstestcomposepoke.R
import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import com.buslaev.siberstestcomposepoke.presentation.app.AppViewModel
import com.buslaev.siberstestcomposepoke.presentation.app.Screens
import com.buslaev.siberstestcomposepoke.presentation.app.Stats

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: AppViewModel,
    isOnline: Boolean
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.loadPokemons(isOnline)
    }
    val state = viewModel.uiState

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.refreshPokemons()
            }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val lazyState = rememberLazyListState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SimpleCheckbox(
                        title = stringResource(id = R.string.attack_title),
                        checked = state.attackChecked
                    ) {
                        viewModel.checkStats(selectedStat = Stats.Attack, it)
                    }
                    SimpleCheckbox(
                        title = stringResource(id = R.string.defense_title),
                        checked = state.defenseChecked
                    ) {
                        viewModel.checkStats(selectedStat = Stats.Defense, it)
                    }
                    SimpleCheckbox(
                        title = stringResource(id = R.string.hp_title),
                        checked = state.hpChecked
                    ) {
                        viewModel.checkStats(selectedStat = Stats.Hp, it)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    state = lazyState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.pokemonList.size) { i ->
                        val item = state.pokemonList[i]
                        if (i >= state.pokemonList.size - 1 && !state.isLoading && isOnline) {
                            println("LOAD MORE")
                            viewModel.loadPokemons(isOnline)
                        }

                        val number = if (state.listOfSelectedStats.isNotEmpty() && i < 3) {
                            val lastChecked = state.listOfSelectedStats[0]
                            when (i) {
                                0 -> Pair(lastChecked, 24)
                                1 -> Pair(lastChecked, 16)
                                2 -> Pair(lastChecked, 12)
                                else -> null
                            }
                        } else null
                        println(number)

                        PokemonItem(pokemon = item, sortedPair = number) { clickedPokemon ->
                            viewModel.pokemonClick(clickedPokemon)
                            navController.navigate(Screens.DetailScreen.route)
                        }
                    }
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun PokemonItem(
    pokemon: Pokemon,
    sortedPair: Pair<Stats, Int>? = null,
    onClick: (Pokemon) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick.invoke(pokemon)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(100.dp, 100.dp)
                    .align(Alignment.Center),
                model = pokemon.spirites.image,
                contentDescription = "image",
                onError = {
                    Icons.Default.Refresh
                }
            )
            sortedPair?.let { pair ->
                Text(
                    text = "${pair.first.name} = ${pokemon.stats.find { it.statNamed.name == pair.first.name }?.value}",
                    modifier = Modifier.align(Alignment.CenterEnd),
                    fontSize = pair.second.sp
                )
            }
        }

        Text(text = pokemon.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}

@Composable
fun SimpleCheckbox(title: String, checked: Boolean, onCheck: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Checkbox(checked = checked, onCheckedChange = {
            onCheck.invoke(it)
        })
    }
}