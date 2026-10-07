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
import com.moukim.shjirati.data.weather.GardenLocationStore
import com.moukim.shjirati.data.weather.WeatherLocation
import com.moukim.shjirati.data.weather.WeatherResult
import com.moukim.shjirati.ui.weather.WeatherScreen
import com.moukim.shjirati.ui.assistant.PlantAssistantScreen
import com.moukim.shjirati.data.weather.PlantWeatherService
import com.moukim.shjirati.data.weather.WeatherRepository
import com.moukim.shjirati.ui.weather.GardenLocationScreen
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
        val weatherRepository = WeatherRepository.from(this)
        setContent {
            ShjiratiTheme {
                ShjiratiApp(repository, catalogRepository, articleRepository, initialPlantId, GardenLocationStore(this), weatherRepository, PlantWeatherService(weatherRepository, this))
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
    targetPlantIdFlow: MutableStateFlow<String?>,
    gardenLocationStore: GardenLocationStore,
    weatherRepository: WeatherRepository,
    plantWeatherService: PlantWeatherService
) {
    var addingPlant by remember { mutableStateOf(false) }
    var editingPlant by remember { mutableStateOf<com.moukim.shjirati.data.local.PlantEntity?>(null) }
    var selectedPlant by remember { mutableStateOf<com.moukim.shjirati.data.local.PlantEntity?>(null) }
    var editingGardenLocation by remember { mutableStateOf(false) }
    var showingWeather by remember { mutableStateOf(false) }
    var showingAssistant by remember { mutableStateOf(false) }
    var weatherOffline by remember { mutableStateOf(false) }
    var weatherRefreshKey by remember { mutableIntStateOf(0) }
    var weatherError by remember { mutableStateOf<String?>(null) }
    var gardenLocation by remember { mutableStateOf(gardenLocationStore.getLocation()) }
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
    val context = LocalContext.current
    vm.attachContext(context)

    val plants by vm.plants.collectAsState()
    val targetPlantId by targetPlantIdFlow.collectAsState()
    val weatherDays by gardenLocation?.let { weatherRepository.observe(it).collectAsState(initial = emptyList()) }
        ?: remember { mutableStateOf(emptyList()) }

    LaunchedEffect(showingWeather, gardenLocation, weatherRefreshKey) {
        if (showingAssistant) {
        PlantAssistantScreen(
            catalogRepository = catalogRepository,
            articleRepository = articleRepository,
            onBack = { showingAssistant = false }
        )
    } else if (showingWeather && gardenLocation != null) {
            weatherError = null
            when (val result = weatherRepository.refresh(gardenLocation!!)) {
                is WeatherResult.Fresh -> weatherOffline = false
                is WeatherResult.Failure -> {
                    weatherOffline = true
                    weatherError = result.message
                }
            }
        }
    }
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

    if (showingWeather && gardenLocation != null) {
        WeatherScreen(
            location = gardenLocation!!,
            days = weatherDays,
            isRefreshing = false,
            isOffline = weatherOffline,
            errorMessage = weatherError,
            onRefresh = {
                weatherError = null
                // Refresh is triggered by toggling the screen state below.
                weatherRefreshKey++
            },
            onBack = { showingWeather = false }
        )
    } else if (editingGardenLocation) {
        GardenLocationScreen(
            initialLocation = gardenLocation,
            onSave = {
                gardenLocationStore.saveLocation(it)
                gardenLocation = it
                editingGardenLocation = false
            },
            onClear = {
                gardenLocationStore.clear()
                gardenLocation = null
            },
            onBack = { editingGardenLocation = false }
        )
    } else if (editingPlant != null) {
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
            weatherAdvice = if (gardenLocation != null) plantWeatherService.observeAdvice(plant.id) else null,
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
            onSelectPlant = { selectedPlant = it },
            onGardenLocation = { editingGardenLocation = true },
            onWeather = { if (gardenLocation != null) showingWeather = true else editingGardenLocation = true },
            onAssistant = { showingAssistant = true }
        )
    }
}
