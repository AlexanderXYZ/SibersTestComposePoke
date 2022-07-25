package com.buslaev.siberstestcomposepoke.presentation.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.buslaev.siberstestcomposepoke.R
import com.buslaev.siberstestcomposepoke.data.models.Pokemon
import java.text.DecimalFormat

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DetailScreen(
    navController: NavController,
    pokemon: Pokemon
) {
    Scaffold(
        topBar = {
            TopAppBar(backgroundColor = Color.Transparent, elevation = 0.dp) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back button")
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(ScrollState(0)),
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                model = pokemon.spirites.image,
                contentDescription = "pokemon image"
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = pokemon.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = stringResource(id = R.string.height_title) + " ${
                    DecimalFormat("#0.0").format(
                        pokemon.height * 0.3048
                    )
                }m"
            )

            Text(text = stringResource(id = R.string.weight_title) + " ${pokemon.weight}kg")

            Spacer(modifier = Modifier.height(16.dp))

            if (pokemon.types.isNotEmpty()) {
                pokemon.types.forEach { type ->
                    Text(text = type.desctiption.name)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pokemon.stats.isNotEmpty()) {
                pokemon.stats.forEach { stat ->
                    Text(text = "${stat.statNamed.name} = ${stat.value}")
                }
            }
        }
    }
}