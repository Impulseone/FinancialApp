package com.darklight_systems.financialapp.view.values_history

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.controller.XmlParser
import com.darklight_systems.financialapp.controller.downloadUrl
import com.darklight_systems.financialapp.model.Currency
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.time.LocalDate


class ValuesHistoryFragment : Fragment() {

    private val URL =
        "http://www.cbr.ru/scripts/XML_dynamic.asp?date_req1=02/03/2020&date_req2=28/05/2020&VAL_NM_RQ=R01235"
    private val GET_ALL_URL = "https://www.cbr.ru/scripts/XML_daily.asp?date_req="
    private lateinit var dateFromButton: Button
    private lateinit var dateToButton: Button
    private lateinit var selectedFromDate: LocalDate
    private lateinit var selectedToDate: LocalDate
    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_values_history, container, false)
        initButtons(view)
        initDates(view)
        initSpinner(view)
        DownloadXmlTask().execute(GET_ALL_URL.plus(parseDate(selectedToDate)))
        return view
    }

    private fun initButtons(view: View?) {
        dateFromButton = (view?.findViewById(R.id.from_btn) as Button)
        dateToButton = (view.findViewById(R.id.to_btn) as Button)
    }

    private fun initDates(view: View?) {
        selectedFromDate = LocalDate.now().minusDays(7)
        selectedToDate = LocalDate.now()
        (view?.findViewById(R.id.from_tv) as TextView).text = parseDate(selectedFromDate)
        (view.findViewById(R.id.to_tv) as TextView).text = parseDate(selectedToDate)
    }

    private fun initSpinner(view: View?) {
        spinner = (view?.findViewById(R.id.spinner) as Spinner)
    }

    private fun parseDate(date: LocalDate): String {
        val parsedDayOfMonth =
            if (date.dayOfMonth < 10) "0${date.dayOfMonth}" else "${date.dayOfMonth}"
        val parsedMonthOfYear =
            if (date.monthValue + 1 < 10) "0${date.monthValue + 1}" else "${date.monthValue + 1}"
        return "${parsedDayOfMonth}/${parsedMonthOfYear}/${date.year}"
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
                val currencyNames: ArrayList<String> = ArrayList()
                for (element in result) {
                    currencyNames.add(element.name)
                }
                val adapter: ArrayAdapter<String>? = context?.let {
                    ArrayAdapter(
                        it,
                        android.R.layout.simple_spinner_item,
                        currencyNames
                    )
                }
                spinner.adapter = adapter
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
}