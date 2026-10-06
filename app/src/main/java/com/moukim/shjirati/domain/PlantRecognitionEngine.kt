package com.moukim.shjirati.domain

import com.moukim.shjirati.data.local.PlantCategory

data class RecognizedPlantInfo(
    val plantId: String,
    val arabicName: String,
    val icon: String,
    val defaultCategory: PlantCategory,
    val defaultIsFruitBearing: Boolean = false,
    val aliases: List<String>
)

object PlantRecognitionEngine {

    const val FALLBACK_ICON = "🌱"

    private val PLANT_DICTIONARY = listOf(
        RecognizedPlantInfo(
            plantId = "apple",
            arabicName = "تفاح",
            icon = "🍎",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("تفاح", "تفاحه", "تفاحة", "توفاح", "تفاح احمر", "تفاح اخضر", "تفاح اصفر", "apple", "apples", "pomme", "pommes", "manzana")
        ),
        RecognizedPlantInfo(
            plantId = "orange",
            arabicName = "برتقال",
            icon = "🍊",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("برتقال", "برتقاله", "برتقالة", "orange", "oranges", "naranja")
        ),
        RecognizedPlantInfo(
            plantId = "olive",
            arabicName = "زيتون",
            icon = "🫒",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("زيتون", "زيتونه", "زيتونة", "olive", "olives", "aceituna")
        ),
        RecognizedPlantInfo(
            plantId = "tomato",
            arabicName = "طماطم",
            icon = "🍅",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("طماطم", "بندورة", "بندوره", "طماطه", "tomato", "tomatoes", "tomate", "tomates")
        ),
        RecognizedPlantInfo(
            plantId = "potato",
            arabicName = "بطاطس",
            icon = "🥔",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("بطاطا", "بطاطس", "potato", "potatoes", "pomme de terre", "patata")
        ),
        RecognizedPlantInfo(
            plantId = "pepper",
            arabicName = "فلفل",
            icon = "🌶️",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("فلفل", "فليفل", "فليفلة", "فلفل حار", "فلفل حلو", "طرشي", "pepper", "peppers", "poivron", "piment")
        ),
        RecognizedPlantInfo(
            plantId = "cucumber",
            arabicName = "خيار",
            icon = "🥒",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("خيار", "خياره", "خيارة", "فقوس", "cucumber", "cucumbers", "concombre")
        ),
        RecognizedPlantInfo(
            plantId = "carrot",
            arabicName = "جزر",
            icon = "🥕",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("جزر", "جزرة", "سفنارية", "carrot", "carrots", "carotte")
        ),
        RecognizedPlantInfo(
            plantId = "eggplant",
            arabicName = "باذنجان",
            icon = "🍆",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("باذنجان", "بتنجان", "بدنجان", "eggplant", "eggplants", "aubergine")
        ),
        RecognizedPlantInfo(
            plantId = "onion",
            arabicName = "بصل",
            icon = "🧅",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("بصل", "بصلة", "بصله", "onion", "onions", "oignon")
        ),
        RecognizedPlantInfo(
            plantId = "garlic",
            arabicName = "ثوم",
            icon = "🧄",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("ثوم", "توم", "ثومة", "garlic", "ail")
        ),
        RecognizedPlantInfo(
            plantId = "corn",
            arabicName = "ذرة",
            icon = "🌽",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("ذرة", "ذره", "دورة", "ذرة صفراء", "corn", "maize", "mais")
        ),
        RecognizedPlantInfo(
            plantId = "mint",
            arabicName = "نعناع",
            icon = "🌿",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("نعناع", "نعنع", "حبق", "ريحان", "زعتر", "بقدونس", "كزبرة", "مقدونس", "كرفس", "شبت", "ميرمية", "mint", "menthe", "basil", "thyme")
        ),
        RecognizedPlantInfo(
            plantId = "lettuce",
            arabicName = "خس",
            icon = "🥬",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("خس", "خسة", "خسه", "ملفوف", "كرنب", "lettuce", "laitue", "cabbage")
        ),
        RecognizedPlantInfo(
            plantId = "lemon",
            arabicName = "ليمون",
            icon = "🍋",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("ليمون", "ليمونة", "ليمونه", "حامض", "قارس", "lemon", "lemons", "citron")
        ),
        RecognizedPlantInfo(
            plantId = "grape",
            arabicName = "عنب",
            icon = "🍇",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("عنب", "عنبة", "دالية", "كرمة", "grape", "grapes", "raisin")
        ),
        RecognizedPlantInfo(
            plantId = "banana",
            arabicName = "موز",
            icon = "🍌",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("موز", "موزة", "موزه", "banana", "bananas", "banane")
        ),
        RecognizedPlantInfo(
            plantId = "palm",
            arabicName = "نخيل",
            icon = "🌴",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("تمر", "نخيل", "نخلة", "نخله", "رطب", "دقلة", "palm", "date", "dattier")
        ),
        RecognizedPlantInfo(
            plantId = "strawberry",
            arabicName = "فراولة",
            icon = "🍓",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("فراولة", "فراوله", "فريز", "strawberry", "strawberries", "fraise")
        ),
        RecognizedPlantInfo(
            plantId = "peach",
            arabicName = "خوخ",
            icon = "🍑",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("خوخ", "خوخة", "خوخه", "مشمش", "مشمشة", "peach", "peaches", "peche", "apricot")
        ),
        RecognizedPlantInfo(
            plantId = "cherry",
            arabicName = "كرز",
            icon = "🍒",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("كرز", "حب الملوك", "cherry", "cherries", "cerise")
        ),
        RecognizedPlantInfo(
            plantId = "watermelon",
            arabicName = "بطيخ",
            icon = "🍉",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("بطيخ", "دلاع", "حبحب", "watermelon", "pasteque")
        ),
        RecognizedPlantInfo(
            plantId = "rose",
            arabicName = "ورد",
            icon = "🌹",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = false,
            aliases = listOf("ورد", "وردة", "ورده", "زهور", "زهرة", "زهره", "ياسمين", "فل", "جوري", "توليب", "rose", "flower", "flowers", "fleur")
        ),
        RecognizedPlantInfo(
            plantId = "mango",
            arabicName = "مانجو",
            icon = "🥭",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("مانجو", "مانجا", "مانجه", "مانغو", "mango", "mangos", "mangue")
        ),
        RecognizedPlantInfo(
            plantId = "pineapple",
            arabicName = "أناناس",
            icon = "🍍",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("اناناس", "أناناس", "pineapple", "ananas")
        ),
        RecognizedPlantInfo(
            plantId = "kiwi",
            arabicName = "كيوي",
            icon = "🥝",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("كيوي", "kiwi")
        ),
        RecognizedPlantInfo(
            plantId = "avocado",
            arabicName = "أفوكادو",
            icon = "🥑",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("افوكادو", "أفوكادو", "avocado", "avocat")
        ),
        RecognizedPlantInfo(
            plantId = "pear",
            arabicName = "كمثرى",
            icon = "🍐",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("كمثرى", "كمثري", "إجاص", "اجاص", "عرموط", "pear", "pears", "poire")
        ),
        RecognizedPlantInfo(
            plantId = "melon",
            arabicName = "شمام / تين",
            icon = "🍈",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("تين", "تينة", "رمان", "رمانة", "شمام", "بطيخ اصفر", "fig", "pomegranate", "melon")
        ),
        RecognizedPlantInfo(
            plantId = "coconut",
            arabicName = "جوز الهند",
            icon = "🥥",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = true,
            aliases = listOf("جوز الهند", "كوكوت", "coconut", "noix de coco")
        ),
        RecognizedPlantInfo(
            plantId = "broccoli",
            arabicName = "بروكلي",
            icon = "🥦",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("بروكلي", "شفلور", "زهرة القرنبيط", "قرنبيط", "broccoli", "brocoli")
        ),
        RecognizedPlantInfo(
            plantId = "mushroom",
            arabicName = "فطر",
            icon = "🍄",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("فطر", "عش الغراب", "فطرة", "mushroom", "champignon")
        ),
        RecognizedPlantInfo(
            plantId = "sunflower",
            arabicName = "دوار الشمس",
            icon = "🌻",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("دوار الشمس", "عباد الشمس", "تباع الشمس", "sunflower", "tournesol")
        ),
        RecognizedPlantInfo(
            plantId = "cactus",
            arabicName = "صبار",
            icon = "🌵",
            defaultCategory = PlantCategory.VEGETABLE,
            defaultIsFruitBearing = false,
            aliases = listOf("صبار", "صباره", "صبارة", "تين شوكي", "cactus")
        ),
        RecognizedPlantInfo(
            plantId = "tree_general",
            arabicName = "شجرة",
            icon = "🌳",
            defaultCategory = PlantCategory.TREE,
            defaultIsFruitBearing = false,
            aliases = listOf("شجرة", "شجره", "سدر", "صنوبر", "حرجية", "tree", "trees", "arbre")
        )
    )

    fun normalizeText(text: String): String {
        return text.trim().lowercase()
            .replace(Regex("[\\u064B-\\u065F]"), "") // Remove Arabic diacritics
            .replace(Regex("[أإآ]"), "ا")
            .replace('ة', 'ه')
            .replace('ى', 'ي')
            .replace(Regex("[ؤئ]"), "و")
            .replace(Regex("[^a-z0-9\\u0621-\\u064A\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun recognize(userInput: String): RecognizedPlantInfo? {
        val clean = normalizeText(userInput)
        if (clean.isBlank()) return null

        // 1. Exact alias match
        PLANT_DICTIONARY.firstOrNull { plant ->
            plant.aliases.any { normalizeText(it) == clean }
        }?.let { return it }

        // 2. Word match (e.g. "شجرة تفاح" or "apple tree" or "red apple")
        val words = clean.split(" ").filter { it.isNotBlank() }
        if (words.size > 1) {
            for (word in words) {
                PLANT_DICTIONARY.firstOrNull { plant ->
                    plant.plantId != "tree_general" && plant.aliases.any { alias -> normalizeText(alias) == word }
                }?.let { return it }
            }
        }

        // 3. Prefix match (if clean length >= 2)
        if (clean.length >= 2) {
            PLANT_DICTIONARY.firstOrNull { plant ->
                plant.aliases.any { alias ->
                    val normAlias = normalizeText(alias)
                    normAlias.startsWith(clean) || clean.startsWith(normAlias)
                }
            }?.let { return it }
        }

        // 4. Substring match (if clean length >= 3)
        if (clean.length >= 3) {
            PLANT_DICTIONARY.firstOrNull { plant ->
                plant.aliases.any { alias ->
                    val normAlias = normalizeText(alias)
                    clean.contains(normAlias) || normAlias.contains(clean)
                }
            }?.let { return it }
        }

        return null
    }

    val POPULAR_ICONS = listOf(
        "🌳", "🍎", "🍊", "🫒", "🍋", "🍇", "🍌", "🌴", "🍑", "🍒", "🌹",
        "🥕", "🍅", "🥔", "🌶️", "🥒", "🍆", "🧅", "🧄", "🌽", "🌿", "🥬",
        "🍓", "🍉", "🥭", "🍍", "🥝", "🥑", "🍐", "🍈", "🥥", "🥦", "🍄", "🌻", "🌵", "🌱"
    )
}
