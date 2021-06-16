package com.darklight_systems.financialapp

import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.darklight_systems.financialapp.model.Currency
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class XmlParserTest {
    @Test
    fun defaultParserTest() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        try {
            val parser: XmlPullParser = appContext.resources.getXml(R.xml.currency_history_example)
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                val TAG = "ЛогКот"
                var tmp = ""
                when (parser.eventType) {
                    XmlPullParser.START_DOCUMENT -> Log.d(TAG, "Начало документа")
                    XmlPullParser.START_TAG -> {
                        Log.d(
                            TAG,
                            "START_TAG: имя тега = " + parser.name
                                    + ", уровень = " + parser.depth
                                    + ", число атрибутов = "
                                    + parser.attributeCount
                        )
                        tmp = ""
                        var i = 0
                        while (i < parser.attributeCount) {
                            tmp = (tmp + parser.getAttributeName(i) + " = "
                                    + parser.getAttributeValue(i) + ", ")
                            i++
                        }
                        if (!TextUtils.isEmpty(tmp)) Log.d(TAG, "Атрибуты: $tmp")
                    }
                    XmlPullParser.END_TAG -> Log.d(TAG, "END_TAG: имя тега = " + parser.name)
                    XmlPullParser.TEXT -> Log.d(TAG, "текст = " + parser.text)
                    else -> {
                    }
                }
                parser.next()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    @Test
    fun customizedParserTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val date = Calendar.getInstance().time
        val parser: XmlPullParser = appContext.resources.getXml(R.xml.currency_history_example)
        val currencyList: ArrayList<Currency> = ArrayList()
        var isValue = false
        var isNominal = false
        try {
            var currency = Currency(0.0, "", 0, date)
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
        }
        for (element in currencyList) {
            println("date: ${element.date}")
        }
        assert(currencyList.size > 0)
    }
}