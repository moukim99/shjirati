package com.moukim.shjirati

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
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
    val context = LocalContext.current
    var notificationPermissionRequested by rememberSaveable { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(vm.plants.value.size) {
        if (
            vm.plants.value.isNotEmpty() &&
            Build.VERSION.SDK_INT >= 33 &&
            !notificationPermissionRequested &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionRequested = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        vm.scheduleAll(LocalContext.current)
    }

    if (addingPlant) {
        PlantFormScreen(
            onSave = { name, category, location, notes, interval, daysMask, hour, minute, seasonalEnabled, spring, summer, autumn, winter ->
                vm.savePlant(name, category, location, notes, interval, daysMask, hour, minute, seasonalEnabled, spring, summer, autumn, winter)
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
