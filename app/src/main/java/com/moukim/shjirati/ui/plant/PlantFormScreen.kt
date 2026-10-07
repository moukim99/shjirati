package com.moukim.shjirati.ui.plant

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moukim.shjirati.data.catalog.PlantCatalogRepository
import com.moukim.shjirati.data.local.PlantCategory
import com.moukim.shjirati.data.local.PlantEntity
import com.moukim.shjirati.domain.PlantAgeCalculator
import com.moukim.shjirati.domain.PlantRecognitionEngine
import com.moukim.shjirati.domain.RecognizedPlantInfo
import com.moukim.shjirati.domain.WateringCalculator
import com.moukim.shjirati.ui.theme.*
import com.moukim.shjirati.util.ImageUtils
import java.io.File
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantFormScreen(
    initialPlant: PlantEntity? = null,
    catalogRepository: PlantCatalogRepository? = null,
    onSave: (
        name: String,
        category: PlantCategory,
        location: String?,
        notes: String?,
        imageUri: String?,
        plantedAt: Long?,
        expectedDate: Long?,
        isFruitBearing: Boolean,
        icon: String?,
        wateringIntervalDays: Int?,
        wateringDaysMask: Int,
        wateringHour: Int,
        wateringMinute: Int,
        seasonalScheduleEnabled: Boolean,
        springIntervalDays: Int?,
        summerIntervalDays: Int?,
        autumnIntervalDays: Int?,
        winterIntervalDays: Int?
    ) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(initialPlant?.name.orEmpty()) }
    var location by remember { mutableStateOf(initialPlant?.location.orEmpty()) }
    var notes by remember { mutableStateOf(initialPlant?.notes.orEmpty()) }
    var imageUris by remember { mutableStateOf(initialPlant?.imageUrisList ?: emptyList()) }
    var plantedAt by remember { mutableStateOf(initialPlant?.plantedAtEpochMillis) }
    var expectedDate by remember { mutableStateOf(initialPlant?.expectedDateEpochMillis) }
    var isFruitBearing by remember { mutableStateOf(initialPlant?.isFruitBearing ?: false) }
    var icon by remember { mutableStateOf(initialPlant?.icon ?: PlantRecognitionEngine.FALLBACK_ICON) }

    var userTouchedCategory by remember { mutableStateOf(initialPlant != null) }
    var userTouchedFruitBearing by remember { mutableStateOf(initialPlant != null) }
    var userTouchedIcon by remember { mutableStateOf(initialPlant?.icon != null) }

    val context = LocalContext.current
    var category by remember { mutableStateOf(initialPlant?.category ?: PlantCategory.TREE) }
    var useWeekdays by remember { mutableStateOf(initialPlant?.wateringDaysMask != 0) }
    var intervalText by remember { mutableStateOf((initialPlant?.wateringIntervalDays ?: 3).toString()) }
    var selectedDays by remember {
        mutableStateOf(
            initialPlant?.wateringDaysMask?.let { mask ->
                DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }.toSet()
            } ?: setOf(LocalDate.now().dayOfWeek)
        )
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var wateringHour by remember { mutableIntStateOf(initialPlant?.wateringHour ?: 18) }
    var wateringMinute by remember { mutableIntStateOf(initialPlant?.wateringMinute ?: 0) }
    var seasonalEnabled by remember { mutableStateOf(initialPlant?.seasonalScheduleEnabled ?: false) }
    var springInterval by remember { mutableStateOf(initialPlant?.springIntervalDays?.toString().orEmpty()) }
    var summerInterval by remember { mutableStateOf(initialPlant?.summerIntervalDays?.toString().orEmpty()) }
    var autumnInterval by remember { mutableStateOf(initialPlant?.autumnIntervalDays?.toString().orEmpty()) }
    var winterInterval by remember { mutableStateOf(initialPlant?.winterIntervalDays?.toString().orEmpty()) }

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }

    var recognizedPlantInfo by remember { mutableStateOf<RecognizedPlantInfo?>(null) }

    // Offline recognition: the curated catalog is authoritative when available;
    // the legacy dictionary remains as a fast fallback for common names.
    LaunchedEffect(name, catalogRepository) {
        if (name.isNotBlank()) {
            val catalogMatch = catalogRepository?.search(name)?.firstOrNull()
            val recognized = PlantRecognitionEngine.recognize(name)
            recognizedPlantInfo = recognized

            if (catalogMatch != null) {
                if (!userTouchedCategory) {
                    category = when (catalogMatch.category) {
                        com.moukim.shjirati.data.catalog.CatalogPlantCategory.TREE ->
                            PlantCategory.TREE
                        com.moukim.shjirati.data.catalog.CatalogPlantCategory.VEGETABLE ->
                            PlantCategory.VEGETABLE
                        com.moukim.shjirati.data.catalog.CatalogPlantCategory.HERB ->
                            PlantCategory.HERB
                    }
                }
                if (!userTouchedFruitBearing && category == PlantCategory.TREE) {
                    isFruitBearing = catalogMatch.dateMode ==
                        com.moukim.shjirati.data.catalog.CatalogDateMode.HARVEST
                }
                if (!userTouchedIcon) {
                    icon = recognized?.icon ?: PlantRecognitionEngine.FALLBACK_ICON
                }
            } else if (recognized != null) {
                icon = recognized.icon
                if (!userTouchedCategory) {
                    category = recognized.defaultCategory
                }
                if (!userTouchedFruitBearing && category == PlantCategory.TREE) {
                    isFruitBearing = recognized.defaultIsFruitBearing
                }
            } else if (!userTouchedIcon && initialPlant == null) {
                icon = PlantRecognitionEngine.FALLBACK_ICON
            }
        } else {
            recognizedPlantInfo = null
            if (!userTouchedIcon && initialPlant == null) {
                icon = PlantRecognitionEngine.FALLBACK_ICON
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) {
            val savedPath = ImageUtils.saveAndCompressImage(context, tempCameraUri!!)
            if (savedPath != null) {
                imageUris = imageUris + savedPath
            }
        }
        tempCameraFile?.delete()
        tempCameraFile = null
        tempCameraUri = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            val newPaths = uris.mapNotNull { uri ->
                ImageUtils.saveAndCompressImage(context, uri)
            }
            if (newPaths.isNotEmpty()) {
                imageUris = imageUris + newPaths
            }
        }
    }

    fun showPlantingDatePicker() {
        val current = plantedAt?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        } ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                plantedAt = LocalDate.of(year, month + 1, day)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    fun showExpectedDatePicker() {
        val current = expectedDate?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        } ?: LocalDate.now().plusMonths(3)
        DatePickerDialog(
            context,
            { _, year, month, day ->
                expectedDate = LocalDate.of(year, month + 1, day)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    val timeState = rememberTimePickerState(
        initialHour = wateringHour,
        initialMinute = wateringMinute,
        is24Hour = true
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    wateringHour = timeState.hour
                    wateringMinute = timeState.minute
                    showTimePicker = false
                }) { Text("تم", color = AppPrimaryBrown, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("إلغاء") }
            },
            title = { Text("وقت التذكير", color = AppTextMain, fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = timeState) }
        )
    }

    if (showImageSourceDialog) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "إضافة صورة النبتة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTextMain,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showImageSourceDialog = false
                            val result = ImageUtils.createTempImageUri(context)
                            if (result != null) {
                                tempCameraFile = result.first
                                tempCameraUri = result.second
                                cameraLauncher.launch(result.second)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = PlantLavender.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PlantAccent, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "التقاط صورة بالكاميرا",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTextMain
                            )
                            Text(
                                text = "استخدام كاميرا الهاتف مباشرة",
                                fontSize = 12.sp,
                                color = PlantMuted
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showImageSourceDialog = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = PlantLavender.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(AppPrimaryBrown, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "اختيار من معرض الصور",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTextMain
                            )
                            Text(
                                text = "اختيار صورة مخزنة سابقاً",
                                fontSize = 12.sp,
                                color = PlantMuted
                            )
                        }
                    }
                }

                if (imageUris.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showImageSourceDialog = false
                                imageUris = emptyList()
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "حذف جميع الصور",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = AppCard.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                shadowElevation = 6.dp,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onBack,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "رجوع",
                            tint = AppPrimaryBrown,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "رجوع",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppPrimaryBrown
                        )
                    }

                    Text(
                        text = if (initialPlant == null) "إضافة نبتة" else "تعديل النبتة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTextMain
                    )

                    Spacer(modifier = Modifier.width(60.dp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Plant Icon Picker / Recognized Icon Avatar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PlantLavender.copy(alpha = 0.6f),
                    border = BorderStroke(2.dp, PlantAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = icon,
                            fontSize = 36.sp
                        )
                    }
                }

                if (recognizedPlantInfo != null && icon == recognizedPlantInfo?.icon) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PlantAccent.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, PlantAccent.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "تم التعرف تلقائياً: ${recognizedPlantInfo?.arabicName} ${recognizedPlantInfo?.icon}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PlantAccent
                            )
                        }
                    }
                } else {
                    Text(
                        text = "أيقونة النبتة (تتعرّف تلقائياً عند كتابة الاسم)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTextMuted
                    )
                }

                // Quick emoji picker list
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PlantRecognitionEngine.POPULAR_ICONS) { itemIcon ->
                        val isSelected = icon == itemIcon
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    icon = itemIcon
                                    userTouchedIcon = true
                                },
                            shape = CircleShape,
                            color = if (isSelected) PlantLavenderActive else Color.White,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PlantAccent else PlantBorderLight
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = itemIcon, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            // Plant Name Input Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "اسم النبتة ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTextMain
                    )
                    Text(
                        text = "*",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PlantWarmAmber
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("مثال: تفاح، طماطم، زيتون...", fontSize = 14.sp, color = PlantMuted.copy(alpha = 0.6f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = PlantMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PlantAccent,
                        unfocusedBorderColor = PlantBorderLight
                    ),
                    singleLine = true
                )
            }

            // Simplified Plant Category Selection (Tree or Vegetable)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "تصنيف النبتة",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTextMain
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlantCategory.entries.forEach { item ->
                        val isSelected = category == item
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable {
                                    category = item
                                    userTouchedCategory = true
                                    if (category == PlantCategory.VEGETABLE) {
                                        isFruitBearing = false
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) PlantLavender else Color.White,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PlantAccent else PlantBorderLight
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = item.arabicLabel(),
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PlantDark else PlantMuted
                                )
                            }
                        }
                    }
                }
            }

            // Fruit-bearing option for Trees
            if (category == PlantCategory.TREE) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, PlantBorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "شجرة مثمرة (تنتج ثماراً)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTextMain
                            )
                            Text(
                                text = "تفعيل هذا الخيار لإضافة تاريخ الحصاد المتوقع",
                                fontSize = 12.sp,
                                color = AppTextMuted
                            )
                        }
                        Switch(
                            checked = isFruitBearing,
                            onCheckedChange = { checked ->
                                isFruitBearing = checked
                                userTouchedFruitBearing = true
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppPrimaryBrown,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = PlantLavenderBorder
                            )
                        )
                    }
                }
            }

            // Planting Date ("تاريخ الغرس") & Dynamic Age Display
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "تاريخ الغرس",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTextMain
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPlantingDatePicker() },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, PlantBorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PlantLavender.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = PlantAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = plantedAt?.let {
                                        val date = Instant.ofEpochMilli(it)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                        "%04d/%02d/%02d".format(date.year, date.monthValue, date.dayOfMonth)
                                    } ?: "اختر تاريخ الغرس",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTextMain
                                )

                                val calculatedAge = remember(plantedAt, category) {
                                    PlantAgeCalculator.calculateAge(plantedAt, category)
                                }
                                if (calculatedAge != null) {
                                    Text(
                                        text = "العمر الحالي: $calculatedAge",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppPrimaryBrown
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = PlantMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expected Date Picker ("التاريخ المتوقع")
            val showExpectedDateField = category != PlantCategory.TREE || isFruitBearing
            if (showExpectedDateField) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val expectedLabel = if (category == PlantCategory.TREE) "تاريخ الحصاد المتوقع" else "تاريخ الإنبات المتوقع"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expectedLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTextMain
                        )
                        if (expectedDate != null) {
                            TextButton(
                                onClick = { expectedDate = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("إلغاء", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showExpectedDatePicker() },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, PlantBorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PlantLavender.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = PlantAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = expectedDate?.let {
                                        val date = Instant.ofEpochMilli(it)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                        "%04d/%02d/%02d".format(date.year, date.monthValue, date.dayOfMonth)
                                    } ?: "اختياري - انقر لتحديد التاريخ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (expectedDate != null) AppTextMain else PlantMuted
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = PlantMuted.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Garden Location Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "مكانها في الحديقة ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTextMain
                    )
                    Text(
                        text = "(اختياري)",
                        fontSize = 12.sp,
                        color = PlantMuted
                    )
                }
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("مثال: الشرفة الجنوبية، الركن المشمس...", fontSize = 14.sp, color = PlantMuted.copy(alpha = 0.6f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PlantMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PlantAccent,
                        unfocusedBorderColor = PlantBorderLight
                    ),
                    singleLine = true
                )
            }

            // Photo Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (imageUris.isEmpty()) "صور النبتة" else "صور النبتة (${imageUris.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTextMain
                    )
                    if (imageUris.isNotEmpty()) {
                        TextButton(
                            onClick = { showImageSourceDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = AppPrimaryBrown,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "إضافة صورة",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppPrimaryBrown
                            )
                        }
                    }
                }

                if (imageUris.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clickable { showImageSourceDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, PlantBorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(PlantLavender.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = PlantAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "إضافة صور للنبتة",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppTextMain
                                    )
                                    Text(
                                        text = "التقاط من الكاميرا أو اختيار عدة صور من المعرض",
                                        fontSize = 12.sp,
                                        color = PlantMuted
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = PlantMuted.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clickable { showImageSourceDialog = true },
                                shape = RoundedCornerShape(16.dp),
                                color = PlantLavender.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, PlantAccent.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "إضافة صورة",
                                        tint = PlantAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "إضافة",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PlantAccent
                                    )
                                }
                            }
                        }

                        items(imageUris.size) { index ->
                            val uriPath = imageUris[index]
                            val bitmap = remember(uriPath) {
                                runCatching { BitmapFactory.decodeFile(uriPath) }.getOrNull()
                            }
                            Box(
                                modifier = Modifier.size(80.dp)
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "صورة $index",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.LightGray, RoundedCornerShape(16.dp))
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(22.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(11.dp))
                                        .clickable {
                                            imageUris = imageUris.filterIndexed { i, _ -> i != index }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "حذف الصورة",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notes Input Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ملاحظات ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTextMain
                    )
                    Text(
                        text = "(اختياري)",
                        fontSize = 12.sp,
                        color = PlantMuted
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("اكتب تعليمات الري، التسميد، أو أي تفاصيل خاصة...", fontSize = 14.sp, color = PlantMuted.copy(alpha = 0.6f)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PlantAccent,
                        unfocusedBorderColor = PlantBorderLight
                    ),
                    minLines = 3
                )
            }

            HorizontalDivider(color = PlantLavenderBorder.copy(alpha = 0.6f), thickness = 1.dp)

            // Schedule Title Section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "جدول السقي",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTextMain
                )
                Text(
                    text = "اختر الطريقة الأسهل لتذكيرك بالسقي.",
                    fontSize = 14.sp,
                    color = AppTextMuted
                )
            }

            // Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { useWeekdays = true },
                    shape = RoundedCornerShape(16.dp),
                    color = if (useWeekdays) AppPrimaryBrown else Color.White.copy(alpha = 0.7f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (useWeekdays) AppPrimaryBrown else PlantLavenderBorder
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "أيام محددة",
                            fontSize = 14.sp,
                            fontWeight = if (useWeekdays) FontWeight.Bold else FontWeight.Medium,
                            color = if (useWeekdays) Color.White else AppTextMuted
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { useWeekdays = false },
                    shape = RoundedCornerShape(16.dp),
                    color = if (!useWeekdays) AppPrimaryBrown else Color.White.copy(alpha = 0.7f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (!useWeekdays) AppPrimaryBrown else PlantLavenderBorder
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "كل عدة أيام",
                            fontSize = 14.sp,
                            fontWeight = if (!useWeekdays) FontWeight.Bold else FontWeight.Medium,
                            color = if (!useWeekdays) Color.White else AppTextMuted
                        )
                    }
                }
            }

            if (useWeekdays) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "اختر أيام السقي",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTextMain
                    )
                    val days = DayOfWeek.entries
                    days.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { day ->
                                val isSelected = day in selectedDays
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clickable {
                                            selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) PlantLavenderActive else Color.White.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, PlantLavenderBorder)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = day.arabicLabel(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) AppTextMain else AppTextMuted
                                        )
                                    }
                                }
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    if (selectedDays.isEmpty()) {
                        Text(
                            text = "اختر يوماً واحداً على الأقل.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { value ->
                        if (value.all(Char::isDigit) && value.length <= 2) intervalText = value
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("السقي كل كم يوم؟", fontSize = 14.sp) },
                    supportingText = { Text("مثال: 3 يعني السقي كل ثلاثة أيام", fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PlantAccent,
                        unfocusedBorderColor = PlantBorderLight
                    ),
                    singleLine = true
                )
            }

            HorizontalDivider(color = PlantLavenderBorder.copy(alpha = 0.6f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تغيير السقي حسب الفصل",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTextMain
                    )
                    Text(
                        text = "اضبط عدد الأيام لكل فصل.",
                        fontSize = 12.sp,
                        color = AppTextMuted
                    )
                }
                Switch(
                    checked = seasonalEnabled,
                    onCheckedChange = { enabled ->
                        seasonalEnabled = enabled
                        if (enabled && springInterval.isBlank() && summerInterval.isBlank() && autumnInterval.isBlank() && winterInterval.isBlank()) {
                            val base = intervalText.toIntOrNull() ?: 3
                            val defaults = WateringCalculator.defaultSeasonalIntervals(base)
                            springInterval = defaults.spring.toString()
                            summerInterval = defaults.summer.toString()
                            autumnInterval = defaults.autumn.toString()
                            winterInterval = defaults.winter.toString()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppPrimaryBrown,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = PlantLavenderBorder
                    )
                )
            }

            if (seasonalEnabled && !useWeekdays) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "السقي كل كم يوم في كل فصل؟",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTextMain
                    )
                    TextButton(
                        onClick = {
                            val base = intervalText.toIntOrNull() ?: 3
                            val defaults = WateringCalculator.defaultSeasonalIntervals(base)
                            springInterval = defaults.spring.toString()
                            summerInterval = defaults.summer.toString()
                            autumnInterval = defaults.autumn.toString()
                            winterInterval = defaults.winter.toString()
                        }
                    ) {
                        Text(
                            text = "تطبيقات تلقائية",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppPrimaryBrown
                        )
                    }
                }
                SeasonField("الربيع", springInterval) { springInterval = it }
                SeasonField("الصيف", summerInterval) { summerInterval = it }
                SeasonField("الخريف", autumnInterval) { autumnInterval = it }
                SeasonField("الشتاء", winterInterval) { winterInterval = it }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clickable { showTimePicker = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, PlantLavenderBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = AppPrimaryBrown,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "وقت التذكير: ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTextMain
                    )
                    Text(
                        text = "%02d:%02d".format(wateringHour, wateringMinute),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTextMain
                    )
                }
            }

            Button(
                onClick = {
                    val interval = intervalText.toIntOrNull()?.coerceIn(1, 99)
                    val mask = if (useWeekdays) {
                        selectedDays.fold(0) { mask, day ->
                            mask or (1 shl (day.value - 1))
                        }
                    } else 0
                    onSave(
                        name.trim(),
                        category,
                        location.trim().ifBlank { null },
                        notes.trim().ifBlank { null },
                        if (imageUris.isEmpty()) null else imageUris.joinToString("|"),
                        plantedAt,
                        if (showExpectedDateField) expectedDate else null,
                        isFruitBearing,
                        icon,
                        if (useWeekdays) null else interval,
                        mask,
                        wateringHour,
                        wateringMinute,
                        seasonalEnabled && !useWeekdays,
                        springInterval.toIntOrNull()?.coerceIn(1, 99),
                        summerInterval.toIntOrNull()?.coerceIn(1, 99),
                        autumnInterval.toIntOrNull()?.coerceIn(1, 99),
                        winterInterval.toIntOrNull()?.coerceIn(1, 99)
                    )
                },
                enabled = name.isNotBlank() &&
                        (useWeekdays && selectedDays.isNotEmpty() || !useWeekdays && intervalText.toIntOrNull()?.let { it > 0 } == true),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppPrimaryBrown,
                    contentColor = Color.White,
                    disabledContainerColor = AppPrimaryBrown.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
            ) {
                Text(
                    text = if (initialPlant == null) "حفظ النبتة" else "حفظ التعديلات",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun PlantCategory.arabicLabel() = when (this) {
    PlantCategory.TREE -> "🌳 شجرة"
    PlantCategory.VEGETABLE -> "🥕 خضروات"
    PlantCategory.HERB -> "🌿 أعشاب"
}

private fun DayOfWeek.arabicLabel() = when (this) {
    DayOfWeek.MONDAY -> "الإثنين"
    DayOfWeek.TUESDAY -> "الثلاثاء"
    DayOfWeek.WEDNESDAY -> "الأربعاء"
    DayOfWeek.THURSDAY -> "الخميس"
    DayOfWeek.FRIDAY -> "الجمعة"
    DayOfWeek.SATURDAY -> "السبت"
    DayOfWeek.SUNDAY -> "الأحد"
}

@Composable
private fun SeasonField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.all(Char::isDigit) && it.length <= 2) onValueChange(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, fontSize = 14.sp) },
        suffix = { Text("يوم", fontSize = 12.sp) },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = PlantAccent,
            unfocusedBorderColor = PlantBorderLight
        ),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
fun PlantFormScreenPreview() {
    ShjiratiTheme {
        PlantFormScreen(
            onSave = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onBack = {}
        )
    }
}
