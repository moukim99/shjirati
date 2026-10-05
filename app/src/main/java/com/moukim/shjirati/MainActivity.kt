package com.moukim.shjirati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moukim.shjirati.ui.theme.ShjiratiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ShjiratiTheme { HomeScreen() } }
    }
}

@Composable
private fun HomeScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("شجيراتي") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("حديقتك", style = MaterialTheme.typography.headlineMedium)
            Text("لا توجد نباتات مضافة بعد.")
            Button(onClick = { }) { Text("إضافة أول نبتة") }
        }
    }
}
