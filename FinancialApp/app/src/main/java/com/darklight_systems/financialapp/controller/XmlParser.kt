package com.darklight_systems.financialapp.controller

import android.content.Context
import android.widget.Toast
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.model.Currency
import org.xmlpull.v1.XmlPullParser

class XmlParser {
    fun parse(context: Context) {
        val currencyList: ArrayList<Currency> = ArrayList()
        var isName = false
        var isValue = false
        var isNominal = false
        try {
            val parser: XmlPullParser = context.resources.getXml(R.xml.contacts)
            var currency = Currency(0.0, "", 0)
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
                        println(parser.name)
                        if (parser.name.equals("Valute")) {
                            currencyList.add(currency)
                            currency = Currency(0.0, "", 0)
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
        for (currency in currencyList) {
            println(currency)
        }
    }
}