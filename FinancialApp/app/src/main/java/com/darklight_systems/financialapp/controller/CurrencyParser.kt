package com.darklight_systems.financialapp.controller

import android.content.Context
import android.util.Xml
import android.widget.Toast
import com.darklight_systems.financialapp.model.Currency
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class CurrencyParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(context:Context, inputStream: InputStream, date: Date): List<Currency> {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return readFeed(context, parser,date)
        }
    }

    private fun readFeed(context: Context, parser:XmlPullParser, date: Date):List<Currency> {
        val currencyList: ArrayList<Currency> = ArrayList()
        var isName = false
        var isValue = false
        var isNominal = false
        try {
            var currency = Currency(0.0, "", 0,date)
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when {
                            parser.name.equals("Name") -> {
                                isName = true
                            }
                            parser.name.equals("Value") -> {
                                isValue = true
                            }
                            parser.name.equals("Nominal") -> {
                                isNominal = true
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        when {
                            isName -> {
                                isName = false
                                currency.name = parser.text
                            }
                            isValue -> {
                                isValue = false
                                currency.value = parser.text.replace(",", ".").toDouble()
                            }
                            isNominal -> {
                                isNominal = false
                                currency.nominal = parser.text.toInt()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("Valute")) {
                            currencyList.add(currency)
                            currency = Currency(0.0, "", 0, date)
                        }
                    }
                    else -> {
                    }
                }
                parser.next()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Toast.makeText(
                context,
                "Ошибка при загрузке XML-документа: $t",
                Toast.LENGTH_LONG
            ).show()
        }
        return currencyList
    }
}