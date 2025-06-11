package com.example.earthquakelocator.viewModel

import android.app.Application
import android.location.Geocoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.earthquakelocator.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
    var lastEarthquake by mutableStateOf<String?>(null)
        private set

    private val geocoder = Geocoder(application, Locale.getDefault())

    init {
        fetchLatestEarthquake()
    }

    private fun fetchLatestEarthquake() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getLatestEarthquake()
                val feature = response.features.firstOrNull()

                feature?.let {
                    val mag = it.properties.mag
                    val placeRaw = it.properties.place
                    val timeMillis = it.properties.time
                    val lat = it.geometry.coordinates[1]
                    val lon = it.geometry.coordinates[0]

                    val cleanedPlace = placeRaw.substringBeforeLast(",").trim()

                    val country = try {
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        addresses?.firstOrNull()?.countryName ?: "nieznany region"
                    } catch (e: Exception) {
                        "nieznany region"
                    }

                    val formattedTime = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault())
                        .format(Date(timeMillis))

                    lastEarthquake = "Ostatni wstrząs: $mag M – $cleanedPlace, $country - $formattedTime"
                }
            } catch (e: Exception) {
                lastEarthquake = "Błąd podczas pobierania danych"
            }
        }
    }
}
