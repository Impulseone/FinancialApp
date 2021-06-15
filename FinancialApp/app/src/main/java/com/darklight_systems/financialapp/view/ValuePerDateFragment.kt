package com.darklight_systems.financialapp.view

import android.app.DatePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.controller.XmlParser
import com.darklight_systems.financialapp.model.Currency
import kotlinx.android.synthetic.main.fragment_value_per_date.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ValuePerDateFragment : Fragment() {

    private val URL = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=14/06/2021"

    private lateinit var selectDateButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_value_per_date, container, false)
        selectDateButton = view?.findViewById(R.id.select_date_button) as Button
        selectDateButton.setOnClickListener {
            openDobPicker(selected_date_tv)
        }
        return view
    }


    private fun openDobPicker(textView: TextView) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireActivity(), { _, year, monthOfYear, dayOfMonth ->
            val date = "${dayOfMonth}/${monthOfYear}/${year}"
            textView.text = date
            DownloadXmlTask().execute(URL)
        }, year, month, day)

        dpd.datePicker.maxDate = Date().time

        dpd.show()
    }

    private inner class DownloadXmlTask : AsyncTask<String, Void, List<Currency>>() {
        override fun doInBackground(vararg urls: String): List<Currency> {
            return try {
                loadXmlFromNetwork(urls[0])
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                emptyList()
            }
        }

        override fun onPostExecute(result: List<Currency>?) {
            if (result != null) {
                for (currency in result) {
                    println(currency)
                }
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun loadXmlFromNetwork(urlString: String): List<Currency> {
        downloadUrl(urlString)?.use { stream ->
            context?.let {
                return XmlParser().parse(it, stream)
            }
        } ?: return emptyList()
    }

    private fun downloadUrl(urlString: String): InputStream? {
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
}