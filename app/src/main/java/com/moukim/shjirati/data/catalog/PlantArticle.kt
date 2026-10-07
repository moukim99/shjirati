package com.moukim.shjirati.data.catalog

data class PlantArticle(
    val plantId: String,
    val description: String,
    val origin: String,
    val plantType: String,
    val propagation: String,
    val soil: String,
    val soilPh: String,
    val sunlight: String,
    val watering: String,
    val temperature: String,
    val frostTolerance: String,
    val heatTolerance: String,
    val plantingSeason: String,
    val plantingMethod: String,
    val seedDepth: String,
    val spacing: String,
    val germination: String,
    val growthHarvest: String,
    val fertilization: String,
    val pestsDiseases: String,
    val notes: String,
    val sourceNotes: String
)
