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
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: AppViewModel,
    isOnline: Boolean
) {
    val state = viewModel.uiState
    val scaffoldState = rememberScaffoldState()

    if (state.isFirstLaunch){
        LaunchedEffect(key1 = Unit) {
            viewModel.obtainEvent(MainEvent.LoadPokemons(isOnline))
        }
    }

    if (state.error.isNotEmpty()) {
        LaunchedEffect(key1 = scaffoldState.snackbarHostState) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = state.error,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.obtainEvent(MainEvent.RefreshPokemonsClicked(isOnline = isOnline))
            }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "refresh icon")
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val lazyState = rememberLazyListState()
            val scope = rememberCoroutineScope()
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
                    ) { isChecked ->
                        viewModel.obtainEvent(
                            MainEvent.CheckBoxClicked(
                                selectedStat = Stats.Attack,
                                isChecked = isChecked
                            )
                        )
                        if (lazyState.firstVisibleItemIndex != 0)
                            scope.launch {
                                lazyState.scrollToItem(0)
                            }
                    }
                    SimpleCheckbox(
                        title = stringResource(id = R.string.defense_title),
                        checked = state.defenseChecked
                    ) { isChecked ->
                        viewModel.obtainEvent(
                            MainEvent.CheckBoxClicked(
                                selectedStat = Stats.Defense,
                                isChecked = isChecked
                            )
                        )
                        if (lazyState.firstVisibleItemIndex != 0)
                            scope.launch {
                                lazyState.scrollToItem(0)
                            }
                    }
                    SimpleCheckbox(
                        title = stringResource(id = R.string.hp_title),
                        checked = state.hpChecked
                    ) { isChecked ->
                        viewModel.obtainEvent(
                            MainEvent.CheckBoxClicked(
                                selectedStat = Stats.Hp,
                                isChecked = isChecked
                            )
                        )
                        if (lazyState.firstVisibleItemIndex != 0)
                            scope.launch {
                                lazyState.scrollToItem(0)
                            }
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
                            viewModel.obtainEvent(MainEvent.LoadPokemons(isOnline))
                        }

                        // Не лучшее место для вычислений
                        val currentStat = if (state.listOfSelectedStats.isNotEmpty() && i < 3) {
                            val stats = state.listOfSelectedStats

                            var maxValue = 0
                            var stateName = stats[0].name
                            stats.forEach { stat ->
                                val value =
                                    item.stats.find { it.statNamed.name == stat.name }?.value
                                value?.let {
                                    if (value > maxValue) {
                                        maxValue = value
                                        stateName = stat.name
                                    }
                                }
                            }
                            when (i) {
                                0 -> Triple(stateName, maxValue, 20)
                                1 -> Triple(stateName, maxValue, 16)
                                2 -> Triple(stateName, maxValue, 12)
                                else -> null
                            }
                        } else null

                        PokemonItem(pokemon = item, stat = currentStat) { clickedPokemon ->
                            viewModel.obtainEvent(MainEvent.PokemonClicked(pokemon = clickedPokemon))
                            navController.navigate(Screens.DetailScreen.route)
                        }
                    }
                    item {
                        if (state.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonItem(
    pokemon: Pokemon,
    stat: Triple<String, Int, Int>? = null,
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
                contentDescription = "pokemon image",
                onError = {
                    Icons.Default.Refresh
                }
            )
            stat?.let { stat ->
                Text(
                    text = "${stat.first} = ${stat.second}",
                    modifier = Modifier.align(Alignment.CenterEnd),
                    fontSize = stat.third.sp
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