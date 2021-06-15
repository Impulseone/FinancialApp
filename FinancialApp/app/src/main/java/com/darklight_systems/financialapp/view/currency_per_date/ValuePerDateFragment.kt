package com.darklight_systems.financialapp.view.currency_per_date

import android.app.DatePickerDialog
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.controller.XmlParser
import com.darklight_systems.financialapp.model.Currency
import kotlinx.android.synthetic.main.fragment_value_per_date.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class ValuePerDateFragment : Fragment() {

    private val URL = "https://www.cbr.ru/scripts/XML_daily.asp?date_req="

    private lateinit var selectDateButton: Button
    private lateinit var currencyAdapter: CurrencyAdapter
    private lateinit var selectedDate: LocalDate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_value_per_date, container, false)
        setSelectDateButton(view)
        setAdapter(view)
        setCurrentDate(view)
        return view
    }

    private fun setCurrentDate(view: View?) {
        selectedDate = LocalDate.now()
        val parsedDayOfMonth =
            if (selectedDate.dayOfMonth < 10) "0${selectedDate.dayOfMonth}" else "${selectedDate.dayOfMonth}"
        val parsedMonthOfYear =
            if (selectedDate.monthValue + 1 < 10) "0${selectedDate.monthValue + 1}" else "${selectedDate.monthValue + 1}"
        val date = "${parsedDayOfMonth}/${parsedMonthOfYear}/${selectedDate.year}"
        (view?.findViewById(R.id.selected_date_tv) as TextView).text = date
        DownloadXmlTask().execute(URL.plus(date))
    }

    private fun setSelectDateButton(view: View?) {
        selectDateButton = view?.findViewById(R.id.select_date_button) as Button
        selectDateButton.setOnClickListener {
            openDatePicker(selected_date_tv)
        }
    }

    private fun setAdapter(view: View?) {
        val recyclerView =
            (view?.findViewById<RecyclerView>(R.id.currency_rv) as RecyclerView)
        currencyAdapter = CurrencyAdapter(ArrayList<Currency>())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = currencyAdapter
    }


    private fun openDatePicker(textView: TextView) {

        val year = selectedDate.year
        val month = selectedDate.monthValue
        val day = selectedDate.dayOfMonth

        val dpd = DatePickerDialog(requireActivity(), { _, year, monthOfYear, dayOfMonth ->
            selectedDate = LocalDate.of(year, monthOfYear, dayOfMonth)
            val parsedDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            val parsedMonthOfYear =
                if (monthOfYear + 1 < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
            val date = "${parsedDayOfMonth}/${parsedMonthOfYear}/${year}"
            textView.text = date
            DownloadXmlTask().execute(URL.plus(date))
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
                currencyAdapter.updateData(result as ArrayList<Currency>)
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