package com.darklight_systems.financialapp.controller

import android.content.Context
import com.darklight_systems.financialapp.model.Currency
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

fun parseFromLocalDateToString(date: LocalDate, pattern: String): String =
    date.format(DateTimeFormatter.ofPattern(pattern))

@Throws(XmlPullParserException::class, IOException::class)
fun loadXmlFromNetwork(urlString: String, xmlParser: XmlParser, context:Context?): ArrayList<Currency> {
    downloadUrl(urlString)?.use { stream ->
        context?.let {
            return xmlParser.parse(
                it,
                stream,
                Date()
            ) as ArrayList<Currency>
        }
    } ?: return ArrayList()
}
