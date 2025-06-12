package com.example.earthquakelocator.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.earthquakelocator.util.EarthquakeResponse
import com.example.earthquakelocator.util.Feature
import com.example.earthquakelocator.util.timeFormatted
import com.example.earthquakelocator.viewModel.EarthquakeViewModel
import com.example.earthquakelocator.util.GeoJsonLinkGenerator
import com.example.earthquakelocator.util.Geometry
import com.example.earthquakelocator.util.Properties

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSearchScreen(
    navController: NavController,
    viewModel: EarthquakeViewModel = viewModel()
) {
    val context = LocalContext.current
    var location by remember { mutableStateOf("") }
    var selectedQuake by remember { mutableStateOf<Feature?>(null) }

    var locationErr by remember { mutableStateOf<String?>(null) }

    val isLoading = viewModel.isLoading
    val results = viewModel.earthquakes
    val error = viewModel.errorMessage

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (error != null) {
        errorMessage = error
        showErrorDialog = true
        viewModel.clearMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szybkie wyszukiwanie") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Wróć"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    locationErr = null
                },
                label = { Text("Miejscowość") },
                isError = locationErr != null,
                supportingText = {
                    locationErr?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (location.isBlank()) {
                        locationErr = "Podaj nazwę miejscowości"
                        return@Button
                    }
                    viewModel.quickSearch(location)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Szukaj")
                }
            }

            if (results.isNotEmpty()) {
                Text(
                    "Znaleziono ${results.size} wstrzęsień ziemi:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                results.forEachIndexed { index, quake ->
                    val props = quake.properties
                    val isSelected = selectedQuake == quake

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedQuake = if (isSelected) null else quake },
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = if (isSelected)
                            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        else
                            CardDefaults.cardColors()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Magnituda: ${props.mag}")
                            Text("Miejsce: ${props.place}")
                            Text("Data: ${props.timeFormatted()}")

                            if (isSelected) {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val url = GeoJsonLinkGenerator.generateLink(
                                            EarthquakeResponse(listOf(quake))
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Text("Pokaż na mapie")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("OK")
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Błąd", color = MaterialTheme.colorScheme.error) },
                text = { Text(errorMessage) }
            )
        }
    }
}
