package com.example.earthquakelocator

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning



import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.earthquakelocator.util.GeoJsonLinkGenerator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.earthquakelocator.network.RetrofitClient
import com.example.earthquakelocator.util.isStartBeforeOrEqualEnd
import com.example.earthquakelocator.util.validateDate
import com.example.earthquakelocator.util.validateDouble
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    EarthquakeForm()
                }
            }
        }
    }
}


@Composable
fun EarthquakeForm() {

    var location  by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate   by remember { mutableStateOf("") }
    var minMag    by remember { mutableStateOf("") }
    var radius    by remember { mutableStateOf("") }
    
    var locationErr  by remember { mutableStateOf<String?>(null) }
    var startErr     by remember { mutableStateOf<String?>(null) }
    var endErr       by remember { mutableStateOf<String?>(null) }
    var magErr       by remember { mutableStateOf<String?>(null) }
    var radiusErr    by remember { mutableStateOf<String?>(null) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun showError(message: String) {
        errorMessage = message
        showErrorDialog = true
    }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Earthquake Locator",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            //      Lokalizacja
            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    locationErr = if (it.isBlank()) "Pole nie może być puste" else null
                },
                label = { Text("Lokalizacja (np. Warszawa)") },
                isError = locationErr != null,
                supportingText = locationErr?.let {
                    {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            //      Data początkowa
            OutlinedTextField(
                value = startDate,
                onValueChange = {
                    startDate = it
                    startErr = validateDate(it)
                },
                label = { Text("Data początkowa (DD-MM-YYYY)") },
                isError = startErr != null,
                supportingText = startErr?.let {
                    {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            //      Data końcowa
            OutlinedTextField(
                value = endDate,
                onValueChange = {
                    endDate = it
                    endErr = validateDate(it)
                },
                label = { Text("Data końcowa (DD-MM-YYYY)") },
                isError = endErr != null,
                supportingText = endErr?.let {
                    {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            //      Minimalna magnituda
            OutlinedTextField(
                value = minMag,
                onValueChange = {
                    minMag = it
                    magErr = validateDouble(it, "Magnituda")
                },
                label = { Text("Minimalna magnituda") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = magErr != null,
                supportingText = magErr?.let {
                    {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            //      Promień
            OutlinedTextField(
                value = radius,
                onValueChange = {
                    radius = it
                    radiusErr = validateDouble(it, "Promień")
                },
                label = { Text("Promień (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = radiusErr != null,
                supportingText = radiusErr?.let {
                    {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Button(onClick = {
                val mag = minMag.toDoubleOrNull()
                val rad = radius.toDoubleOrNull()

                if (location.isBlank() || startDate.isBlank() || endDate.isBlank() || mag == null || mag < 0 || rad == null || rad < 0) {
                    showError("Uzupełnij poprawnie wszystkie pola")
                    return@Button
                }

                if (!isStartBeforeOrEqualEnd(startDate, endDate)) {
                    showError("Data początkowa musi być wcześniejsza lub równa dacie końcowej")
                    return@Button
                }

                val geocoder = Geocoder(context)
                val addresses = geocoder.getFromLocationName(location, 1)

                if (addresses.isNullOrEmpty()) {
                    showError("Nie znaleziono lokalizacji")
                    return@Button
                }

                val lat = addresses[0].latitude
                val lon = addresses[0].longitude

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.api.getEarthquakes(
                            start = startDate,
                            end = endDate,
                            minMag = mag,
                            lat = lat,
                            lon = lon,
                            radius = rad
                        )

                        withContext(Dispatchers.Main) {
                            if (response.features.isEmpty()) {
                                showError("Brak wyników dla podanych danych")
                            } else {
                                val url = GeoJsonLinkGenerator.generateLink(response)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showError("Błąd sieci: ${e.message}")
                        }
                    }
                }
            }) {
                Text("Pokaż na mapie")
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
}


@Preview(showBackground = true)
@Composable
fun EarthquakeFormPreview() {
    EarthquakeForm()
}
