package com.darklight_systems.financialapp.controller

import android.content.Context
import com.darklight_systems.financialapp.R

import org.xmlpull.v1.XmlPullParser


class XmlParser {
    fun parse(context: Context) {
        val parser: XmlPullParser = context.resources.getXml(R.xml.contacts)
        val list: ArrayList<String> = ArrayList()
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG
                && parser.name.equals("contact")
            ) {
                list.add(
                    parser.getAttributeValue(0) + " "
                            + parser.getAttributeValue(1) + "\n"
                            + parser.getAttributeValue(2)
                )
            }
            parser.next()
        }
        for (element in list) {
            println(element)
        }
    }
}