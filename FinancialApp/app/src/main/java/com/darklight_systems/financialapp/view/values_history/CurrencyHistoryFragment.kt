package com.darklight_systems.financialapp.view.values_history

import android.app.DatePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.controller.CurrencyParser
import com.darklight_systems.financialapp.controller.convertToDateFromLocalDate
import com.darklight_systems.financialapp.controller.downloadUrl
import com.darklight_systems.financialapp.model.Currency
import com.darklight_systems.financialapp.model.GET_ALL_CURRENCY_URL
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class CurrencyHistoryFragment : Fragment() {

    private lateinit var dateFromButton: Button
    private lateinit var dateToButton: Button
    private lateinit var selectedFromDate: LocalDate
    private lateinit var selectedToDate: LocalDate
    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_currency_history, container, false)
        initButtons(view)
        initDates(view)
        initSpinner(view)
        DownloadCurrencyTask().execute(
            GET_ALL_CURRENCY_URL(
                parseFromLocalDateToString(
                    selectedToDate
                )
            )
        )
        return view
    }

    private fun initButtons(view: View?) {
        dateFromButton = (view?.findViewById(R.id.from_btn) as Button)
        dateFromButton.setOnClickListener {
            openDatePicker(
                (view.findViewById(R.id.from_tv) as TextView),
                selectedFromDate,
                true
            )
        }
        dateToButton = (view.findViewById(R.id.to_btn) as Button)
        dateToButton.setOnClickListener {
            openDatePicker(
                (view.findViewById(R.id.to_tv) as TextView),
                selectedToDate,
                false
            )
        }
    }

    private fun openDatePicker(textView: TextView, date: LocalDate, isFromDate: Boolean) {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        val dpd = DatePickerDialog(requireActivity(), { _, year, monthOfYear, dayOfMonth ->
            if (isFromDate) selectedFromDate = LocalDate.of(year, monthOfYear, dayOfMonth)
            else selectedToDate = LocalDate.of(year, monthOfYear, dayOfMonth)
            val parsedDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            val parsedMonthOfYear =
                if (monthOfYear < 10) "0${monthOfYear}" else "$monthOfYear"
            val date = "${parsedDayOfMonth}/${parsedMonthOfYear}/${year}"
            textView.text = date
        }, year, month, day)

        dpd.datePicker.maxDate = Date().time
        dpd.show()
    }

    private fun initDates(view: View?) {
        selectedFromDate = LocalDate.now().minusDays(7)
        selectedToDate = LocalDate.now()
        (view?.findViewById(R.id.from_tv) as TextView).text =
            parseFromLocalDateToString(selectedFromDate)
        (view.findViewById(R.id.to_tv) as TextView).text =
            parseFromLocalDateToString(selectedToDate)
    }

    private fun initSpinner(view: View?) {
        spinner = (view?.findViewById(R.id.spinner) as Spinner)
    }

    private fun drawGraph(view: View?, dataList: ArrayList<Pair<Date, Double>>) {
        val graph = view?.findViewById(R.id.graph) as GraphView
        val dataArray: Array<DataPoint?> = arrayOfNulls(dataList.size)
        for ((index, element) in dataList.withIndex()) dataArray[index] =
            DataPoint(element.first, element.second)
        val series: LineGraphSeries<DataPoint> = LineGraphSeries(dataArray)
        graph.addSeries(series)

        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(activity);
        graph.gridLabelRenderer.numHorizontalLabels = 3

        graph.viewport.setMinX(convertToDateFromLocalDate(selectedFromDate).time.toDouble())
        graph.viewport.setMaxX(convertToDateFromLocalDate(selectedToDate).time.toDouble())
        graph.viewport.isXAxisBoundsManual = true

        graph.gridLabelRenderer.setHumanRounding(false)
    }

    private fun parseFromLocalDateToString(date: LocalDate): String {
        val parsedDayOfMonth =
            if (date.dayOfMonth < 10) "0${date.dayOfMonth}" else "${date.dayOfMonth}"
        val parsedMonthOfYear =
            if (date.monthValue + 1 < 10) "0${date.monthValue + 1}" else "${date.monthValue + 1}"
        return "${parsedDayOfMonth}/${parsedMonthOfYear}/${date.year}"
    }

    private inner class DownloadCurrencyTask : AsyncTask<String, Void, List<Currency>>() {
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

        @Throws(XmlPullParserException::class, IOException::class)
        private fun loadXmlFromNetwork(urlString: String): List<Currency> {
            downloadUrl(urlString)?.use { stream ->
                context?.let {
                    return CurrencyParser().parse(it, stream, convertToDateFromLocalDate(selectedFromDate))
                }
            } ?: return emptyList()
        }
    }

    private inner class DownloadCurrencyHistoryTask : AsyncTask<String, Void, Void>() {

        override fun doInBackground(vararg url: String?): Void? {
            return null
        }

        override fun onPostExecute(result: Void?) {
//            drawGraph()
        }

    }
}