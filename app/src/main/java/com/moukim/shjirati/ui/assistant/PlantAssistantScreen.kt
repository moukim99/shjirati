package com.moukim.shjirati.ui.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moukim.shjirati.assistant.LocalJebRuleRunner
import com.moukim.shjirati.assistant.PlantAssistantResponse
import com.moukim.shjirati.assistant.PlantAssistantResponseBuilder
import com.moukim.shjirati.assistant.PlantAssistantResolver
import com.moukim.shjirati.data.catalog.PlantArticleRepository
import com.moukim.shjirati.data.catalog.PlantCatalogRepository
import kotlinx.coroutines.launch

@Composable
fun PlantAssistantScreen(
    catalogRepository: PlantCatalogRepository,
    articleRepository: PlantArticleRepository,
    onBack: () -> Unit
) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<PlantAssistantResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val runner = remember { LocalJebRuleRunner() }
    val resolver = remember { PlantAssistantResolver(catalogRepository) }
    val builder = remember { PlantAssistantResponseBuilder() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مساعد شجيرتي", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("اسأل عن أي نبتة موجودة في قاعدة شجيرتي، ويعمل هذا المساعد دون إنترنت.")
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مثال: كيف أسقي الطماطم؟") },
                minLines = 3
            )
            Button(
                onClick = {
                    if (question.isBlank() || isLoading) return@Button
                    scope.launch {
                        isLoading = true
                        val decision = runner.decide(question)
                        val plant = resolver.resolvePlant(question)
                        answer = builder.build(
                            decision = decision,
                            plant = plant,
                            article = plant?.let { articleRepository.getByPlantId(it.id) }
                        )
                        isLoading = false
                    }
                },
                enabled = question.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Text(if (isLoading) "جارٍ البحث..." else "اسأل")
            }
            when (val result = answer) {
                is PlantAssistantResponse.Answer -> Text(result.text)
                is PlantAssistantResponse.NeedClarification -> Text(result.text)
                null -> Unit
            }
        }
    }
}
