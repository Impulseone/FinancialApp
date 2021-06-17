package com.darklight_systems.financialapp.controller

import android.content.Context
import com.darklight_systems.financialapp.model.Currency
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.util.*

interface XmlParser {
    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(context: Context, inputStream: InputStream, date: Date): List<Currency>
}