package com.moukim.shjirati

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moukim.shjirati.data.PlantRepositoryImpl
import com.moukim.shjirati.data.catalog.PlantCatalogRepository
import com.moukim.shjirati.data.local.DatabaseProvider
import com.moukim.shjirati.notifications.WateringAlarmReceiver
import com.moukim.shjirati.ui.home.HomeScreen
import com.moukim.shjirati.ui.home.HomeViewModel
import com.moukim.shjirati.ui.plant.PlantDetailScreen
import com.moukim.shjirati.ui.plant.PlantFormScreen
import com.moukim.shjirati.ui.theme.ShjiratiTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val initialPlantId = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialPlantId.value = intent?.getStringExtra(WateringAlarmReceiver.EXTRA_PLANT_ID)
        val repository = PlantRepositoryImpl(DatabaseProvider.get(this).dao())
        val catalogRepository = PlantCatalogRepository(this)
        val articleRepository = com.moukim.shjirati.data.catalog.PlantArticleRepository(this)
        setContent {
            ShjiratiTheme {
                ShjiratiApp(repository, catalogRepository, articleRepository, initialPlantId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(WateringAlarmReceiver.EXTRA_PLANT_ID)?.let { id ->
            initialPlantId.value = id
        }
    }
}

@Composable
private fun ShjiratiApp(
    repository: PlantRepositoryImpl,
    catalogRepository: PlantCatalogRepository,
    articleRepository: com.moukim.shjirati.data.catalog.PlantArticleRepository,
    targetPlantIdFlow: MutableStateFlow<String?>
) {
    var addingPlant by remember { mutableStateOf(false) }
    var editingPlant by remember { mutableStateOf<com.moukim.shjirati.data.local.PlantEntity?>(null) }
    var selectedPlant by remember { mutableStateOf<com.moukim.shjirati.data.local.PlantEntity?>(null) }
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
    val context = LocalContext.current
    vm.attachContext(context)

    val plants by vm.plants.collectAsState()
    val targetPlantId by targetPlantIdFlow.collectAsState()

    LaunchedEffect(plants, targetPlantId) {
        val targetId = targetPlantId
        if (targetId != null && plants.isNotEmpty()) {
            val found = plants.find { it.id == targetId }
            if (found != null) {
                selectedPlant = found
                targetPlantIdFlow.value = null
            }
        }
    }

    var notificationPermissionRequested by rememberSaveable { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(plants.size) {
        if (
            plants.isNotEmpty() &&
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
        vm.scheduleAll(context)
    }

    if (editingPlant != null) {
        val plant = editingPlant!!
        PlantFormScreen(
            initialPlant = plant,
            catalogRepository = catalogRepository,
            onSave = { name, category, location, notes, imageUri, plantedAt, expectedDate, isFruitBearing, icon, interval, daysMask, hour, minute, seasonalEnabled, spring, summer, autumn, winter ->
                vm.updatePlant(plant, name, category, location, notes, imageUri, plantedAt, expectedDate, isFruitBearing, icon, interval, daysMask, hour, minute, seasonalEnabled, spring, summer, autumn, winter)
                editingPlant = null
            },
            onBack = { editingPlant = null }
        )
    } else if (selectedPlant != null) {
        val plant = selectedPlant!!
        PlantDetailScreen(
            plant = plant,
            article = articleRepository.getByPlantId(plant.id),
            history = repository.observeWateringHistory(plant.id),
            onBack = { selectedPlant = null },
            onWater = { vm.water(plant) },
            onEdit = { editingPlant = plant; selectedPlant = null },
            onDelete = { vm.deletePlant(context, plant); selectedPlant = null }
        )
    } else if (addingPlant) {
        PlantFormScreen(
            catalogRepository = catalogRepository,
            onSave = { name, category, location, notes, imageUri, plantedAt, expectedDate, isFruitBearing, icon, interval, daysMask, hour, minute, seasonalEnabled, spring, summer, autumn, winter ->
                vm.savePlant(name, category, location, notes, imageUri, plantedAt, expectedDate, isFruitBearing, icon, interval, daysMask, hour, minute, seasonalEnabled, spring, summer, autumn, winter)
                addingPlant = false
            },
            onBack = { addingPlant = false }
        )
    } else {
        HomeScreen(
            plants = plants,
            onAddPlant = { addingPlant = true },
            onWater = vm::water,
            onSelectPlant = { selectedPlant = it }
        )
    }
}
