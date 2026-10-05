package com.moukim.shjirati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moukim.shjirati.data.PlantRepositoryImpl
import com.moukim.shjirati.data.local.DatabaseProvider
import com.moukim.shjirati.ui.home.HomeScreen
import com.moukim.shjirati.ui.home.HomeViewModel
import com.moukim.shjirati.ui.plant.PlantFormScreen
import com.moukim.shjirati.ui.theme.ShjiratiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = PlantRepositoryImpl(DatabaseProvider.get(this).dao())
        setContent {
            ShjiratiTheme {
                ShjiratiApp(repository)
            }
        }
    }
}

@Composable
private fun ShjiratiApp(repository: PlantRepositoryImpl) {
    var addingPlant by remember { mutableStateOf(false) }
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))

    if (addingPlant) {
        PlantFormScreen(
            onSave = { name, category, location, notes, interval, daysMask, hour, minute ->
                vm.savePlant(name, category, location, notes, interval, daysMask, hour, minute)
                addingPlant = false
            },
            onBack = { addingPlant = false }
        )
    } else {
        HomeScreen(
            plants = vm.plants.collectAsState().value,
            onAddPlant = { addingPlant = true },
            onWater = vm::water
        )
    }
}
