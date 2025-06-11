package com.example.earthquakelocator.util

data class Geometry(
    val coordinates: List<Double>
)

data class Properties(
    val mag: Double,
    val place: String,
    val time: Long
)

data class Feature(
    val properties: Properties,
    val geometry: Geometry
)

data class EarthquakeResponse(
    val features: List<Feature>
)