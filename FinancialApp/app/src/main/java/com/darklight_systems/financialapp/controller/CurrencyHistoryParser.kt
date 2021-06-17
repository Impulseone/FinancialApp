package com.darklight_systems.financialapp.controller

import android.content.Context
import android.util.Xml
import android.widget.Toast
import com.darklight_systems.financialapp.model.Currency
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CurrencyHistoryParser: XmlParser{

    @Throws(XmlPullParserException::class, IOException::class)
    override fun parse(context: Context, inputStream: InputStream, date: Date): List<Currency> {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return readFeed(context, parser)
        }
    }

    private fun readFeed(context: Context, parser: XmlPullParser): List<Currency> {
        val currencyList: ArrayList<Currency> = ArrayList()
        var isValue = false
        var isNominal = false
        try {
            var currency = Currency("",0.0, "", 0, Calendar.getInstance().time)
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when {
                            parser.name.equals("Record") -> {
                                val dateString: String = parser.getAttributeValue(0)
                                currency.date = SimpleDateFormat("dd.MM.yyyy").parse(dateString)
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
                        if (parser.name.equals("Record")) {
                            currencyList.add(currency)
                            currency = Currency("",0.0, "", 0, Calendar.getInstance().time)
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