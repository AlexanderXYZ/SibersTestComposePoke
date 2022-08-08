package com.buslaev.siberstestcomposepoke.presentation.main

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import com.buslaev.siberstestcomposepoke.presentation.app.PokemonStatUi
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

    if (state.isFirstLaunch) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val listState = rememberLazyGridState()
            val scope = rememberCoroutineScope()

            TopBarCheckedBoxes(
                attackChecked = state.attackChecked,
                defenseChecked = state.defenseChecked,
                hpChecked = state.hpChecked,
                checkBoxClicked = { stat, isChecked ->
                    viewModel.obtainEvent(
                        MainEvent.CheckBoxClicked(
                            selectedStat = stat,
                            isChecked = isChecked
                        )
                    )
                    if (listState.firstVisibleItemIndex != 0)
                        scope.launch {
                            listState.scrollToItem(0)
                        }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PokemonListContent(
                listState = listState,
                pokemonList = state.pokemonList,
                listOfSelectedStats = state.listOfSelectedStats,
                isLoading = state.isLoading,
                isOnline = isOnline,
                loadMore = { viewModel.obtainEvent(MainEvent.LoadPokemons(isOnline)) },
                pokemonClicked = { pokemon ->
                    viewModel.obtainEvent(MainEvent.PokemonClicked(pokemon = pokemon))
                    navController.navigate(Screens.DetailScreen.route)
                }
            )
        }
    }
}

@Composable
fun TopBarCheckedBoxes(
    attackChecked: Boolean,
    defenseChecked: Boolean,
    hpChecked: Boolean,
    checkBoxClicked: (Stats, Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SimpleCheckbox(
            title = stringResource(id = R.string.attack_title),
            checked = attackChecked
        ) { isChecked ->
            checkBoxClicked.invoke(Stats.Attack, isChecked)
        }
        SimpleCheckbox(
            title = stringResource(id = R.string.defense_title),
            checked = defenseChecked
        ) { isChecked ->
            checkBoxClicked.invoke(Stats.Defense, isChecked)
        }
        SimpleCheckbox(
            title = stringResource(id = R.string.hp_title),
            checked = hpChecked
        ) { isChecked ->
            checkBoxClicked.invoke(Stats.Hp, isChecked)
        }
    }
}

@Composable
fun PokemonListContent(
    listState: LazyGridState,
    pokemonList: List<Pokemon>,
    listOfSelectedStats: List<Stats>,
    isLoading: Boolean = true,
    isOnline: Boolean,
    loadMore: () -> Unit,
    pokemonClicked: (Pokemon) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(pokemonList.size) { i ->
            val item = pokemonList[i]
            if (i >= pokemonList.size - 1 && !isLoading && isOnline)
                loadMore.invoke()

            val currentStat = if (listOfSelectedStats.isNotEmpty() && i < 3) {
                var maxValue = 0
                var stateName = listOfSelectedStats[0].name
                listOfSelectedStats.forEach { stat ->
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
                    0 -> PokemonStatUi(stateName, maxValue, 20)
                    1 -> PokemonStatUi(stateName, maxValue, 16)
                    2 -> PokemonStatUi(stateName, maxValue, 12)
                    else -> null
                }
            } else null

            PokemonItem(pokemon = item, stat = currentStat) { clickedPokemon ->
                pokemonClicked.invoke(clickedPokemon)
            }
        }
        item {
            if (isLoading)
                SimpleCircularProgressIndicator()
        }
    }
}

@Composable
fun PokemonItem(
    pokemon: Pokemon,
    stat: PokemonStatUi? = null,
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
        AsyncImage(
            modifier = Modifier
                .size(100.dp),
            model = pokemon.spirites.image,
            contentDescription = "pokemon image",
        )

        Text(text = pokemon.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)

        stat?.let { stat ->
            Text(
                text = "${stat.title} = ${stat.value}",
                fontSize = stat.fontSize.sp
            )
        }
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

@Composable
fun SimpleCircularProgressIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}