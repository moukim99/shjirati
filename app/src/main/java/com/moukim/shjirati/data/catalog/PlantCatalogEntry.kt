package com.moukim.shjirati.data.catalog

enum class CatalogPlantCategory { TREE, VEGETABLE, HERB }
enum class CatalogDateMode { HARVEST, GERMINATION }

data class PlantCatalogEntry(
    val id: String,
    val arabicName: String,
    val aliases: List<String>,
    val scientificName: String,
    val category: CatalogPlantCategory,
    val dateMode: CatalogDateMode,
    val imageAsset: String
)
