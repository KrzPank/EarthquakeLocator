package com.example.earthquakelocator.util

import android.util.Log
import com.example.earthquakelocator.model.EarthquakeResponse
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object GeoJsonLinkGenerator {

    fun generateLink(response: EarthquakeResponse): String {
        val geoJson = mapOf(
            "type" to "FeatureCollection",
            "features" to response.features.map { feature ->
                mapOf(
                    "type" to "Feature",
                    "properties" to mapOf(
                        "mag" to feature.properties.mag,
                        "place" to feature.properties.place,
                        "time" to feature.properties.time
                    ),
                    "geometry" to mapOf(
                        "type" to "Point",
                        "coordinates" to feature.geometry.coordinates
                    )
                )
            }
        )

        val json = Gson().toJson(geoJson)
        val encoded = URLEncoder.encode(json, StandardCharsets.UTF_8.toString())

        val url = "https://geojson.io/#data=data:application/json,$encoded"

        //Log.d("EQuake: geoJson", json)
        //Log.d("EQuake: url", url)
        return url
    }
}