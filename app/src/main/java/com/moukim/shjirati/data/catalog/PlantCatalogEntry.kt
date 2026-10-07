package com.moukim.shjirati.data.catalog

enum class CatalogPlantCategory { TREE, VEGETABLE, HERB }
enum class CatalogDateMode { HARVEST, GERMINATION }

data class CatalogGrowingData(
    val germinationDaysMin: Int? = null,
    val germinationDaysMax: Int? = null,
    val maturityDaysMin: Int? = null,
    val maturityDaysMax: Int? = null,
    val source: String? = null
)

data class PlantCatalogEntry(
    val id: String,
    val arabicName: String,
    val aliases: List<String>,
    val scientificName: String,
    val category: CatalogPlantCategory,
    val dateMode: CatalogDateMode,
    val imageAsset: String,
    val growingData: CatalogGrowingData? = null
)
