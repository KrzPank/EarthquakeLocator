package com.example.earthquakelocator.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.earthquakelocator.util.isStartBeforeOrEqualEnd
import com.example.earthquakelocator.viewModel.EarthquakeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeFormScreen(
    navController: NavController,
    viewModel: EarthquakeViewModel = viewModel()
) {
    val context = LocalContext.current

    var location by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var minMag by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("") }

    var locationErr by remember { mutableStateOf<String?>(null) }
    var startErr by remember { mutableStateOf<String?>(null) }
    var endErr by remember { mutableStateOf<String?>(null) }
    var magErr by remember { mutableStateOf<String?>(null) }
    var radiusErr by remember { mutableStateOf<String?>(null) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val loading = viewModel.isLoading
    val url = viewModel.geoJsonUrl

    LaunchedEffect(url) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            context.startActivity(intent)
            viewModel.clearMessages()
        }
    }

    viewModel.errorMessage?.let {
        errorMessage = it
        showErrorDialog = true
        viewModel.clearMessages()
    }

    fun validateAndFetch() {
        locationErr = null
        startErr = null
        endErr = null
        magErr = null
        radiusErr = null

        var isValid = true

        if (location.isBlank()) {
            locationErr = "Podaj lokalizację"
            isValid = false
        }
        if (startDate.isBlank()) {
            startErr = "Podaj datę początkową"
            isValid = false
        }
        if (endDate.isBlank()) {
            endErr = "Podaj datę końcową"
            isValid = false
        }
        val mag = minMag.toDoubleOrNull()
        if (mag == null || mag < 0 || mag > 10) {
            magErr = "Nieprawidłowa magnituda"
            isValid = false
        }
        val rad = radius.toDoubleOrNull()
        if (rad == null || rad < 0) {
            radiusErr = "Nieprawidłowy promień"
            isValid = false
        }

        if (isValid && !isStartBeforeOrEqualEnd(startDate, endDate)) {
            startErr = "Niepoprawny zakres dat"
            endErr = "Niepoprawny zakres dat"
            isValid = false
        }

        if (isValid) {
            viewModel.fetchEarthquakesFromForm(
                location = location,
                startDate = startDate,
                endDate = endDate,
                minMag = minMag,
                radius = radius
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zobacz na mapie") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Lokalizacja
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Lokalizacja") },
                isError = locationErr != null,
                supportingText = { locationErr?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Start
            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Data początkowa (DD-MM-YYYY)") },
                isError = startErr != null,
                supportingText = { startErr?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            // End
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("Data końcowa (DD-MM-YYYY)") },
                isError = endErr != null,
                supportingText = { endErr?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Magnituda
            OutlinedTextField(
                value = minMag,
                onValueChange = { minMag = it },
                label = { Text("Minimalna magnituda") },
                isError = magErr != null,
                supportingText = { magErr?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Promień
            OutlinedTextField(
                value = radius,
                onValueChange = { radius = it },
                label = { Text("Promień [km]") },
                isError = radiusErr != null,
                supportingText = { radiusErr?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Przycisk
            Button(
                onClick = { validateAndFetch() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Pokaż na mapie")
                }
            }
        }

        // Globalny alert
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

