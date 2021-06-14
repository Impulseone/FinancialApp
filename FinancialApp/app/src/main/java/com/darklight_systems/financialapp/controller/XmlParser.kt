package com.darklight_systems.financialapp.controller

import android.content.Context
import android.widget.Toast
import com.darklight_systems.financialapp.R
import org.xmlpull.v1.XmlPullParser

class XmlParser {

    fun parseV2(context: Context) {
        val namesList: ArrayList<String> = ArrayList()
        val valuesList: ArrayList<String> = ArrayList()
        var isName = false
        var isValue = false
        try {
            val parser: XmlPullParser = context.resources.getXml(R.xml.contacts)
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                val TAG = "ЛогКот"
                var tmp = ""
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name.equals("Name")) {
                            isName = true
                        }
                        if (parser.name.equals("Value")) {
                            isValue = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (isName) {
                            namesList.add(parser.text)
                            isName = false
                        }
                        if (isValue) {
                            valuesList.add(parser.text)
                            isValue = false
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
        for (name in namesList) {
            println(name)
        }
        for (value in valuesList) {
            println(value)
        }
    }
}