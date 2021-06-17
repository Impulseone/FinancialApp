package com.darklight_systems.financialapp.view

import android.app.DatePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.darklight_systems.financialapp.R
import com.darklight_systems.financialapp.controller.AllCurrenciesParser
import com.darklight_systems.financialapp.controller.convertToDateFromLocalDate
import com.darklight_systems.financialapp.controller.downloadUrl
import com.darklight_systems.financialapp.controller.parseFromLocalDateToString
import com.darklight_systems.financialapp.model.CURRENCY_HISTORY_URL
import com.darklight_systems.financialapp.model.Currency
import com.darklight_systems.financialapp.model.GET_ALL_CURRENCY_URL
import kotlinx.android.synthetic.main.fragment_currency_per_date.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class ConverterFragment : Fragment() {

    private var selectedFromCurrency: String = ""
    private var selectedToCurrency: String = ""
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private var allCurrencies:ArrayList<Currency> = ArrayList()
    private lateinit var selectedDate: LocalDate
    private lateinit var selectDateButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_converter, container, false)
        setCurrentDate(view)
        initSpinners(view)
        setSelectDateButton(view)
        return view
    }

    private fun setCurrentDate(view: View?) {
        selectedDate = LocalDate.now()
        val date = parseFromLocalDateToString(selectedDate,"dd/MM/yyyy")
        (view?.findViewById(R.id.selected_date_tv) as TextView).text = date
        DownloadCurrencyTask().execute(GET_ALL_CURRENCY_URL(parseFromLocalDateToString(LocalDate.now(),"dd.MM.yyyy")))
    }

    private fun initSpinners(view: View) {
        spinnerFrom = view.findViewById<Spinner>(R.id.from_currency_spinner)
        spinnerFrom.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedFromCurrency = (spinnerFrom.selectedItem as String)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerTo = view.findViewById<Spinner>(R.id.to_currency_spinner)
        spinnerTo.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedToCurrency = spinnerTo.selectedItem as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setSelectDateButton(view: View) {
        selectDateButton = view.findViewById(R.id.select_date_button) as Button
        selectDateButton.setOnClickListener {
            openDatePicker(selected_date_tv)
        }
    }

    private fun openDatePicker(textView: TextView) {
        val year = selectedDate.year
        val month = selectedDate.monthValue-1
        val day = selectedDate.dayOfMonth
        val dpd = DatePickerDialog(requireActivity(), { _, year, monthOfYear, dayOfMonth ->
            selectedDate = LocalDate.of(year, monthOfYear+1, dayOfMonth)
            val dateString = parseFromLocalDateToString(selectedDate,"dd/MM/yyyy")
            textView.text = dateString
        },
            year, month, day)

        dpd.datePicker.maxDate = Date().time

        dpd.show()
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
                spinnerFrom.adapter = adapter
                spinnerTo.adapter = adapter
            }
        }

        @Throws(XmlPullParserException::class, IOException::class)
        private fun loadXmlFromNetwork(urlString: String): List<Currency> {
            downloadUrl(urlString)?.use { stream ->
                context?.let {
                    return AllCurrenciesParser().parse(
                        it,
                        stream,
                        convertToDateFromLocalDate(LocalDate.now())
                    )
                }
            } ?: return emptyList()
        }
    }
}