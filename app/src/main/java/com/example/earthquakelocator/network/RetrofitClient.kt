package com.example.earthquakelocator.network

import com.example.earthquakelocator.util.EarthquakeResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object RetrofitClient {
    private const val BASE_URL = "https://earthquake.usgs.gov/fdsnws/event/1/"

    private fun getClient(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: EarthquakeService by lazy {
        getClient().create(EarthquakeService::class.java)
    }
}


interface EarthquakeService {
    @GET("query")
    suspend fun getEarthquakes(
        @Query("format") format: String = "geojson",
        @Query("starttime") start: String,
        @Query("endtime") end: String,
        @Query("minmagnitude") minMag: Double,
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("maxradiuskm") radius: Double
    ): EarthquakeResponse

    @GET("query")
    suspend fun getLatestEarthquake(
        @Query("format") format: String = "geojson",
        @Query("orderby") orderBy: String = "time",
        @Query("limit") limit: Int = 1
    ): EarthquakeResponse
}
