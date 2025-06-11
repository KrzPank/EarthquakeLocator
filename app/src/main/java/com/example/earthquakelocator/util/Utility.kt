package com.example.earthquakelocator.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

fun Properties.timeFormatted(): String {
    val date = Date(this.time)
    val format = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault())
    return format.format(date)
}
