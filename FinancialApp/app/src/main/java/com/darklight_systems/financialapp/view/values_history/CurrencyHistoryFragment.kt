package com.darklight_systems.financialapp.view.values_history

import android.app.DatePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class CurrencyHistoryFragment : Fragment() {

    private lateinit var dateFromButton: Button
    private lateinit var dateToButton: Button
    private lateinit var selectedFromDate: LocalDate
    private lateinit var selectedToDate: LocalDate
    private lateinit var spinner: Spinner
    private var selectedCurrencyCode: String = "R01235"
    private var allCurrencies: ArrayList<Currency> = ArrayList()

    private var graphId = View.generateViewId()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_currency_history, container, false)
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
        DownloadCurrencyHistoryTask(view).execute(
            CURRENCY_HISTORY_URL(
                parseFromLocalDateToString(selectedFromDate, "dd/MM/yyyy"),
                parseFromLocalDateToString(selectedToDate, "dd/MM/yyyy"), selectedCurrencyCode
            )
        )
        return view
    }

    private fun initDates(view: View) {
        selectedFromDate = LocalDate.now().minusDays(7)
        selectedToDate = LocalDate.now()
        (view.findViewById(R.id.from_tv) as TextView).text =
            parseFromLocalDateToString(selectedFromDate, "dd/MM/yyyy")
        (view.findViewById(R.id.to_tv) as TextView).text =
            parseFromLocalDateToString(selectedToDate, "dd/MM/yyyy")
    }

    private fun initButtons(view: View) {
        dateFromButton = (view.findViewById(R.id.from_btn) as Button)
        dateFromButton.setOnClickListener {
            openDatePicker(
                (view.findViewById(R.id.from_tv) as TextView),
                selectedFromDate,
                true,
                view
            )
        }
        dateToButton = (view.findViewById(R.id.to_btn) as Button)
        dateToButton.setOnClickListener {
            openDatePicker(
                (view.findViewById(R.id.to_tv) as TextView),
                selectedToDate,
                false,
                view
            )
        }
    }

    private fun openDatePicker(
        textView: TextView,
        date: LocalDate,
        isFromDate: Boolean,
        view: View
    ) {
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
            DownloadCurrencyHistoryTask(view).execute(
                CURRENCY_HISTORY_URL(
                    parseFromLocalDateToString(selectedFromDate, "dd/MM/yyyy"),
                    parseFromLocalDateToString(selectedToDate, "dd/MM/yyyy"),
                    selectedCurrencyCode
                )
            )
        }, year, month, day)

        dpd.datePicker.maxDate = Date().time
        dpd.show()
    }

    private fun initSpinner(fragmentView: View) {
        spinner = (fragmentView.findViewById(R.id.spinner) as Spinner)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedCurrencyCode = findCurrencyCodeByName(spinner.selectedItem as String)
                DownloadCurrencyHistoryTask(fragmentView).execute(
                    CURRENCY_HISTORY_URL(
                        parseFromLocalDateToString(selectedFromDate, "dd/MM/yyyy"),
                        parseFromLocalDateToString(selectedToDate, "dd/MM/yyyy"),
                        selectedCurrencyCode
                    )
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun findCurrencyCodeByName(name: String): String {
        var currencyCode: String = ""
        for (element in allCurrencies) {
            if (element.name == name) {
                currencyCode = element.id
                break
            }
        }
        return currencyCode
    }

    private fun createGraph(view: View, dataList: ArrayList<Currency>) {
        val graph: GraphView = GraphView(view.context)
        graph.id = graphId

        val dataArray: Array<DataPoint?> = arrayOfNulls(dataList.size)
        for ((index, element) in dataList.withIndex()) dataArray[index] =
            DataPoint(element.date, element.value)
        val series: LineGraphSeries<DataPoint> = LineGraphSeries(dataArray)
        graph.addSeries(series)
        graph.gridLabelRenderer.labelFormatter =
            DateAsXAxisLabelFormatter(activity, SimpleDateFormat("dd.MM.yy"))
        graph.gridLabelRenderer.numHorizontalLabels = 4
        graph.gridLabelRenderer.setHumanRounding(false)

        val layout = view.findViewById(R.id.history_layout) as LinearLayout
        layout.addView(graph)
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
                allCurrencies = result as ArrayList<Currency>
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
                    return AllCurrenciesParser().parse(
                        it,
                        stream,
                        convertToDateFromLocalDate(selectedFromDate)
                    )
                }
            } ?: return emptyList()
        }
    }

    private inner class DownloadCurrencyHistoryTask(private val view: View) :
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
            val layout = view.findViewById(R.id.history_layout) as LinearLayout
            layout.removeView(view.findViewById(graphId))
            createGraph(view, result)
        }

    }
}