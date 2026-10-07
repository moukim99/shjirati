package com.moukim.shjirati.data.weather

enum class PlantWeatherRisk {
    NONE, FROST, HEAT, FROST_AND_HEAT
}

data class PlantWeatherAdvice(
    val plantId: String,
    val dateEpochDay: Long,
    val risk: PlantWeatherRisk,
    val message: String
)

object PlantWeatherAdvisor {
    fun advise(
        plantId: String,
        day: WeatherDaily,
        profile: PlantWeatherProfile?
    ): PlantWeatherAdvice {
        if (profile == null) {
            return PlantWeatherAdvice(
                plantId, day.dateEpochDay, PlantWeatherRisk.NONE,
                "لا تتوفر حدود خاصة بهذا النبات للطقس بعد."
            )
        }

        val frost = day.temperatureMinC?.let { it <= profile.frostRiskMinC } == true
        val heat = day.temperatureMaxC?.let { it >= profile.heatRiskMaxC } == true

        val risk = when {
            frost && heat -> PlantWeatherRisk.FROST_AND_HEAT
            frost -> PlantWeatherRisk.FROST
            heat -> PlantWeatherRisk.HEAT
            else -> PlantWeatherRisk.NONE
        }

        val message = when (risk) {
            PlantWeatherRisk.FROST ->
                "تنبيه: الحرارة الدنيا المتوقعة قد تعرض هذه النبتة لخطر الصقيع. يُنصح بحمايتها."
            PlantWeatherRisk.HEAT ->
                "تنبيه: الحرارة القصوى المتوقعة قد تعرض هذه النبتة لإجهاد حراري. راقب الري والظل."
            PlantWeatherRisk.FROST_AND_HEAT ->
                "تنبيه: توجد مخاطر برد وحرارة مرتفعة لهذه النبتة ضمن بيانات الطقس."
            PlantWeatherRisk.NONE ->
                "الطقس المتوقع ضمن الحدود الإرشادية المسجلة لهذه النبتة."
        }

        return PlantWeatherAdvice(plantId, day.dateEpochDay, risk, message)
    }

    fun adviseAll(
        plantId: String,
        days: List<WeatherDaily>,
        profile: PlantWeatherProfile?
    ): List<PlantWeatherAdvice> =
        days.map { advise(plantId, it, profile) }
}
