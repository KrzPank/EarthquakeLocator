package com.example.earthquakelocator.util

import java.text.SimpleDateFormat
import java.util.Locale

fun validateDate(s: String): String? {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    dateFormat.isLenient = false
    return try {
        dateFormat.parse(s)
        null  // OK
    } catch (e: Exception) {
        "Niepoprawna data"
    }
}

fun validateDouble(s: String, field: String): String? {
    val value = s.toDoubleOrNull()
    return if (value == null || value < 0) {
        "$field musi być liczbą > 0"
    } else {
        null
    }
}

fun isStartBeforeOrEqualEnd(startDate: String, endDate: String): Boolean {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    dateFormat.isLenient = false
    return try {
        val start = dateFormat.parse(startDate)
        val end = dateFormat.parse(endDate)
        if (start != null && end != null) {
            !start.after(end)
        } else false
    } catch (e: Exception) {
        false
    }
}
