package com.darklight_systems.financialapp.controller

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

fun downloadUrl(urlString: String): InputStream? {
    val url = URL(urlString)
    return (url.openConnection() as? HttpURLConnection)?.run {
        readTimeout = 10000
        connectTimeout = 15000
        requestMethod = "GET"
        doInput = true
        // Starts the query
        connect()
        inputStream
    }
}

fun convertToDateFromLocalDate(dateToConvert: LocalDate): Date {
    return Date.from(
        dateToConvert.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant()
    )
}

fun parseFromLocalDateToString(date: LocalDate): String {
    val parsedDayOfMonth =
        if (date.dayOfMonth < 10) "0${date.dayOfMonth}" else "${date.dayOfMonth}"
    val parsedMonthOfYear =
        if (date.monthValue < 10) "0${date.monthValue}" else "${date.monthValue}"
    return "${parsedDayOfMonth}/${parsedMonthOfYear}/${date.year}"
}