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
import com.darklight_systems.financialapp.controller.*
import com.darklight_systems.financialapp.model.CURRENCY_HISTORY_URL
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
        initDates(view)
        initButtons(view)
        initSpinner(view)
        DownloadCurrencyTask().execute(
            GET_ALL_CURRENCY_URL(
                parseFromLocalDateToString(
                    selectedToDate,
                    "dd/MM/yyyy"
                )
            )
        )
        DownloadCurrencyHistoryTask().execute(
            CURRENCY_HISTORY_URL(
                parseFromLocalDateToString(selectedFromDate, "dd/MM/yyyy"),
                parseFromLocalDateToString(selectedToDate, "dd/MM/yyyy"), "R01235"
            )
        )

        return view
    }

    private fun initDates(view: View?) {
        selectedFromDate = LocalDate.now().minusDays(7)
        selectedToDate = LocalDate.now()
        (view?.findViewById(R.id.from_tv) as TextView).text =
            parseFromLocalDateToString(selectedFromDate, "dd/MM/yyyy")
        (view.findViewById(R.id.to_tv) as TextView).text =
            parseFromLocalDateToString(selectedToDate, "dd/MM/yyyy")
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
        val month = date.monthValue - 1
        val day = date.dayOfMonth

        val dpd = DatePickerDialog(requireActivity(), { _, year, monthOfYear, dayOfMonth ->
            if (isFromDate) selectedFromDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
            else selectedToDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
            val date = if (isFromDate) parseFromLocalDateToString(
                selectedFromDate,
                "dd/MM/yyyy"
            ) else parseFromLocalDateToString(selectedToDate, "dd/MM/yyyy")
            textView.text = date
        }, year, month, day)

        dpd.datePicker.maxDate = Date().time
        dpd.show()
    }

    private fun initSpinner(view: View?) {
        spinner = (view?.findViewById(R.id.spinner) as Spinner)
    }

    private fun drawGraph(view: View?, dataList: ArrayList<Currency>) {
        val graph = view?.findViewById(R.id.graph) as GraphView
        val dataArray: Array<DataPoint?> = arrayOfNulls(dataList.size)
        for ((index, element) in dataList.withIndex()) dataArray[index] =
            DataPoint(element.date, element.value)
        val series: LineGraphSeries<DataPoint> = LineGraphSeries(dataArray)
        graph.addSeries(series)

        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(activity);
        graph.gridLabelRenderer.numHorizontalLabels = 3
        graph.viewport.setMinX(convertToDateFromLocalDate(selectedFromDate).time.toDouble())
//        graph.viewport.setMaxX(convertToDateFromLocalDate(selectedToDate).time.toDouble())
//        graph.viewport.isXAxisBoundsManual = true
//        graph.gridLabelRenderer.setHumanRounding(false)
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
                    return CurrencyParser().parse(
                        it,
                        stream,
                        convertToDateFromLocalDate(selectedFromDate)
                    )
                }
            } ?: return emptyList()
        }
    }

    private inner class DownloadCurrencyHistoryTask :
        AsyncTask<String, Void, ArrayList<Currency>>() {

        override fun doInBackground(vararg url: String): ArrayList<Currency> {
            return try {
                loadXmlFromNetwork(url[0])
            } catch (e: IOException) {
                e.printStackTrace()
                ArrayList()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                ArrayList()
            }
        }


        @Throws(XmlPullParserException::class, IOException::class)
        private fun loadXmlFromNetwork(urlString: String): ArrayList<Currency> {
            downloadUrl(urlString)?.use { stream ->
                context?.let {
                    return CurrencyHistoryParser().parse(
                        it,
                        stream
                    ) as ArrayList<Currency>
                }
            } ?: return ArrayList()
        }

        override fun onPostExecute(result: ArrayList<Currency>) {
            drawGraph(view, result)
        }

    }
}