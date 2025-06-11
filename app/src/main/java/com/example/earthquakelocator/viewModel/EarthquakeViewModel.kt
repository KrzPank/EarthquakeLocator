package com.example.earthquakelocator.viewModel

import android.app.Application
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.earthquakelocator.network.RetrofitClient
import com.example.earthquakelocator.util.Feature
import com.example.earthquakelocator.util.GeoJsonLinkGenerator
import com.example.earthquakelocator.util.isStartBeforeOrEqualEnd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EarthquakeViewModel(application: Application) : AndroidViewModel(application) {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var geoJsonUrl by mutableStateOf<String?>(null)
        private set

    var earthquakes by mutableStateOf<List<Feature>>(emptyList())

    private val context = getApplication<Application>().applicationContext

    fun fetchEarthquakesFromForm(
        location: String,
        startDate: String,
        endDate: String,
        minMag: String,
        radius: String
    ) {
        val mag = minMag.toDoubleOrNull()
        val rad = radius.toDoubleOrNull()

        if (location.isBlank() || startDate.isBlank() || endDate.isBlank() || mag == null || mag < 0 || rad == null || rad < 0) {
            errorMessage = "Uzupełnij poprawnie wszystkie pola"
            return
        }

        if (!isStartBeforeOrEqualEnd(startDate, endDate)) {
            errorMessage = "Data początkowa musi być wcześniejsza lub równa dacie końcowej"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context)
                val addresses = geocoder.getFromLocationName(location, 1)

                if (addresses.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Nie znaleziono lokalizacji"
                    }
                    return@launch
                }

                val lat = addresses[0].latitude
                val lon = addresses[0].longitude

                isLoading = true
                val response = RetrofitClient.api.getEarthquakes(
                    start = startDate,
                    end = endDate,
                    minMag = mag,
                    lat = lat,
                    lon = lon,
                    radius = rad
                )

                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (response.features.isEmpty()) {
                        errorMessage = "Brak wyników dla podanych danych"
                    } else {
                        geoJsonUrl = GeoJsonLinkGenerator.generateLink(response)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = "Błąd: ${e.localizedMessage}"
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun quickSearch(location: String) {
        val endDate = LocalDate.now()
        val startDate = endDate.minusMonths(6)
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context)
                val addresses = geocoder.getFromLocationName(location, 1)

                if (addresses.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Nie znaleziono lokalizacji"
                    }
                    return@launch
                }

                val lat = addresses[0].latitude
                val lon = addresses[0].longitude

                isLoading = true
                val response = RetrofitClient.api.getEarthquakes(
                    start = startDate.format(formatter),
                    end = endDate.format(formatter),
                    minMag = 1.0,
                    lat = lat,
                    lon = lon,
                    radius = 500.0
                )

                withContext(Dispatchers.Main) {
                    isLoading = false
                    earthquakes = response.features
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = "Błąd: ${e.localizedMessage}"
                }
            }
        }
    }


    fun clearMessages() {
        errorMessage = null
        geoJsonUrl = null
    }
}